package controllers;

import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import mapper.MdMapper;
import mapper.MdMapper.MdAdScale;
import mapper.MdMapper.MdAdspace;
import play.Logger;
import play.Logger.ALogger;
import play.cache.Cache;
import play.libs.F.Function;
import play.libs.F.Promise;
import play.libs.Json;
import play.libs.ws.WS;
import play.libs.ws.WSResponse;
import play.mvc.Controller;
import play.mvc.Result;
import protocbuf.MgxBidReq.MgxBidRequest;
import protocbuf.MgxBidReq.MgxBidRequest.AdSlot;
import protocbuf.MgxBidReq.MgxBidRequest.Mobile;
import protocbuf.MgxBidResp.MgxBidResponse;

import com.fasterxml.jackson.databind.JsonNode;

import entity.MdResp;

public class Application extends Controller {
	// static final ExecutionContext myExecutionContext =
	// Akka.system().dispatchers().lookup("my-context");
	private static MdMapper mdMapper = MdMapper.getInstance();
	// private static MdMapper mdMapper;
	private static final Lock lock = new ReentrantLock();

	private static final ALogger logger = Logger.of("controllers");

	public static <T> Promise<Result> index() {
		try {
			final MgxBidRequest req = MgxBidRequest.parseFrom(request().body()
					.asRaw().asBytes());
			Logger.debug("Tosreq:" + req.toString());
			final AdSlot adSlot = req.getAdslot(0);
			final Mobile mobile = req.getMobile();
			MdAdspace adspace = mdMapper.adspaceMap.get(adSlot.getMegaxAid()
					+ "");
			final String adspaceid = adspace.getAdsapceId();// "CE61F9458CE9CC33";
			final String bidKey = adspaceid + System.currentTimeMillis();
			float width = (float) adSlot.getAdsWidth();
			float height = (float) adSlot.getAdsHeight();
			if(6==adSlot.getViewType());{
				String rs = mobile.getDeviceRs();
				if(rs!=null){
					String[] wh = rs.toUpperCase().split("X");
					if(wh.length==2){
						try {
							width = Float.parseFloat(wh[0]);
							height = Float.parseFloat(wh[1]);
						} catch (Exception e) {
							// TODO: handle exception
						}
					}
					
				}
				
			}
				
			MdAdScale adaptAdscale = adaptAdscale(adspace.getScales(), width
					/ height, height);
			StringBuffer sb = new StringBuffer(
					"http://ad.sandbox.madserving.com/adcall/bidrequest?");
			sb.append("adspaceid=");// 需要对应
			sb.append(adspaceid);
			sb.append("&width=");
			sb.append(adaptAdscale.getWidth());
			sb.append("&height=");
			sb.append(adaptAdscale.getHeight());
			sb.append("&pid=2");
			sb.append("&pcat=13");
			// sb.append(adSlot.getMediaType(0));
			sb.append("&media=1");
			sb.append("&adtype=");// 需要mapping ok
			sb.append(mdMapper.adtypeMap.get(adSlot.getViewType() + ""));//
			sb.append("&bid=");// 需要按规则生成
			lock.lock();
			Object o = Cache.get(bidKey);
			if (o == null) {
				o = "0001";
				Cache.set(bidKey, o, 1);
			} else {
				String v = (String) o;
				Integer intV = Integer.parseInt(v);
				intV++;
				String nv = intV + "";
				int length = nv.length();
				for (int i = 0; i < 4 - length; i++) {
					nv = "0" + nv;
				}
				o = nv;
				Cache.set(bidKey, o, 1);
			}
			final String bid = (String)o;
			lock.unlock();
			sb.append(bidKey + o);
			sb.append("&ip=");
			sb.append(req.getIp());
			sb.append(mdMapper.deviceIdMap.get(mobile.getDeviceUniqueType()
					+ ""));
			sb.append(mobile.getDeviceUniqueId());

			String os = mobile.getDeviceOs();
			if (os != null) {
				String[] osArr = os.split(" ");
				sb.append("&os=");
				sb.append(getOsNum(os));
				sb.append("&osv=");// 需要解析
				if (osArr.length > 1)
					sb.append(osArr[1]);
			}

			sb.append("&ua=");// 需要解析
			sb.append(URLEncoder.encode(req.getUserAgent(), "UTF-8"));
			sb.append("&appname=");// 需要解析
			sb.append(adspace.getAppName());
			sb.append("&pkgname=");// 需要解析
			sb.append(adspace.getPkgName());
			sb.append("&debug=");// 需要解析
			if (req.getIsTest())
				sb.append("1");
			else
				sb.append("0");

			sb.append("&conn=");
			sb.append(mdMapper.deviceNetMap.get(mobile.getDeviceNet() + ""));// 需要mapping
			sb.append("&carrier=");
			sb.append(mdMapper.carrierMap.get(mobile
					.getDeviceCommunicationOperatorsId() + ""));// 需要mapping
			sb.append("&apitype=2");// json模式
			sb.append("&density=");
			sb.append(mobile.getDeviceRs());
			// sb.append("&cell=2");no mapping
			sb.append("&device=");
			sb.append(URLEncoder.encode(mobile.getDeviceBrand(), "UTF-8"));
			sb.append("&lat=");
			sb.append(mobile.getLatitude());
			sb.append("&lon=");
			sb.append(mobile.getLongitude());
			Logger.debug("mdreq:" + sb.toString());
			final long start = System.currentTimeMillis();
			Promise<WSResponse> resp = WS.url(sb.toString()).get();
			Promise<Result> s = resp.map(new Function<WSResponse, Result>() {

				@Override
				public Result apply(WSResponse resp) throws Throwable {
					// TODO Auto-generated method stub
					long end = System.currentTimeMillis();
					JsonNode no = resp.asJson();
					Logger.debug("mdresp:" + no.toString());
					JsonNode v = no.get(adspaceid);
					MdResp mdResp = Json.fromJson(v, MdResp.class);
					logger.debug((end-start)+","+mdResp.getReturncode()+","+adspaceid+","+bidKey+bid+","+mobile.getDeviceUniqueId());
					MgxBidResponse.Builder mgxResp = MgxBidResponse
							.newBuilder();
					mgxResp.setBidGuid(req.getBidGuid());
					mgxResp.setVersion(req.getVersion());
					if (mdResp.getReturncode() == 200) {
						protocbuf.MgxBidResp.MgxBidResponse.AdSlot.Builder adSlotResp = protocbuf.MgxBidResp.MgxBidResponse.AdSlot
								.newBuilder();
						adSlotResp.setId(1);
						adSlotResp.setMegaxAid(adSlot.getMegaxAid());
						adSlotResp.setMaxCpmMicros(500);
						if (adSlot.getBuyerIdCount() > 0) {
							adSlotResp.setBuyerId(adSlot.getBuyerId(0));
						} else {
							adSlotResp.setBuyerId("12345678900");
						}
						adSlotResp.setAdvertiserId("10013909");
						adSlotResp.setCreativeFormat(1);
						adSlotResp.setCreativeContent(mdResp.getImgurl());
						adSlotResp.setCreativeHeight(mdResp.getAdheight());
						adSlotResp.setCreativeWidth(mdResp.getAdwidth());
						adSlotResp.setClickThroughUrl(mdResp.getClickurl());
						String[] imgtrackings = mdResp.getImgtracking();
						if (imgtrackings != null) {
							adSlotResp.addAllImpressionTracking(Arrays
									.asList(imgtrackings));

						}
						String[] thclUrls = mdResp.getThclkurl();
						if (thclUrls != null) {
							adSlotResp.addAllClickTracking(Arrays
									.asList(thclUrls));

						}
						adSlotResp.setBidfloor(500);
						mgxResp.addAdslot(adSlotResp);
					}
					MgxBidResponse result = mgxResp.build();
					Logger.debug("tosresp:" + result.toString());
					response().setContentType("application/octet-stream");
					return ok(result.toByteArray());
				}
			});

			return s;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private static MdAdScale adaptAdscale(List<MdAdScale> adscales,
			float newAdscale, float newHeight) {
		float preV = 0;
		int vIndex = 0;
		boolean comm = false;
		for (int i = 0, size = adscales.size(); i < size; i++) {
			MdAdScale os = adscales.get(i);
			float adScale = os.getScale();
			float curV = Math.abs(newAdscale - adScale);
			if (i == 0) {
				preV = curV;
			} else {
				float minV = Math.min(preV, curV);

				if (minV == preV) {
					if (preV == curV) {
						comm = true;
						vIndex = (Math
								.abs((adscales.get(vIndex).getFheight() - newHeight)) < Math
								.abs((adscales.get(i).getFheight() - newHeight))) ? vIndex
								: i;
					} else {
						if (comm)
							break;
						comm = false;
						vIndex = i - 1;
						break;
					}
				} else {
					preV = minV;
					if (i == size - 1) {
						vIndex = i;
					}
				}
			}

		}
		return adscales.get(vIndex);
	}

	private static int getOsNum(String os) {
		String upos = os.toUpperCase();
		if (upos.contains("ANDROID")) {
			return 0;
		} else if (upos.contains("IOS")) {
			return 1;
		}
		if (upos.contains("WINDOWS")) {
			return 2;
		} else {
			return 3;
		}
	}
	// public static Result index() throws InvalidProtocolBufferException{
	// final MgxBidRequest req =
	// MgxBidRequest.parseFrom(request().body().asRaw().asBytes());
	// MgxBidResponse.Builder mgxResp = MgxBidResponse.newBuilder();
	// final AdSlot adSlot = req.getAdslot(0);
	// mgxResp.setBidGuid(req.getBidGuid());
	// mgxResp.setVersion(req.getVersion());
	// if(200==200){
	// protocbuf.MgxBidResp.MgxBidResponse.AdSlot.Builder adSlotResp =
	// protocbuf.MgxBidResp.MgxBidResponse.AdSlot.newBuilder();
	// adSlotResp.setId(1);
	// adSlotResp.setMegaxAid(adSlot.getMegaxAid());
	// adSlotResp.setMaxCpmMicros(500);
	// if(adSlot.getBuyerIdCount()>0){
	// adSlotResp.setBuyerId(adSlot.getBuyerId(0));
	// }else{
	// adSlotResp.setBuyerId("12345678900");
	// }
	// adSlotResp.setAdvertiserId("12345678901");
	// adSlotResp.setCreativeFormat(1);
	// adSlotResp.setCreativeContent("http://123123213213");
	// adSlotResp.setCreativeHeight(100);
	// adSlotResp.setCreativeWidth(640);
	// adSlotResp.setClickThroughUrl("http://123123213213");
	// String [] imgtrackings = new String[]{"http://123123213213"};
	// if(imgtrackings!=null){
	// adSlotResp.addAllImpressionTracking(Arrays.asList(imgtrackings));
	//
	// }
	// String [] thclUrls = new String[]{"http://123123213213"};
	// if(thclUrls!=null){
	// adSlotResp.addAllClickTracking(Arrays.asList(thclUrls));
	//
	// }
	// adSlotResp.setBidfloor(500);
	// mgxResp.addAdslot(adSlotResp);
	// }
	// MgxBidResponse result = mgxResp.build();
	// Logger.debug("tosresp:"+result.toString());
	// response().setContentType("application/octet-stream");
	// return ok(result.toByteArray());
	// }

}
