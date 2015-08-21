package mapper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import play.libs.Json;

public class MdMapper {
	//存放tos的广告类型对应md的广告类型
	private static MdMapper instance = new MdMapper();
	public final  Map<String,String> adtypeMap;
	public final  Map<String,String> deviceIdMap;
	public final  Map<String,String> deviceNetMap;
	public final  Map<String,String> carrierMap;
	public final Map<String,MdAdspace> adspaceMap;
	private MdMapper(){
		HashMap<String,String> modifiableAdtypeMap = new HashMap<String,String>();
		modifiableAdtypeMap.put("4","2");
		modifiableAdtypeMap.put("6","2");
		modifiableAdtypeMap.put("7","2");
		
		HashMap<String,String> modifiableDeviceIdMap = new HashMap<String,String>();
		modifiableDeviceIdMap.put("1","&wma=");
		modifiableDeviceIdMap.put("2","&imei=");
		modifiableDeviceIdMap.put("3","&aid=");
		modifiableDeviceIdMap.put("4","&idfa=");
		modifiableDeviceIdMap.put("5","&oid=");
		//deviceIdMap.put("6","5");vdid(Vendor 标示符,仅适用于 iOS) no mapping
		modifiableDeviceIdMap.put("7","&uid=");
		//终端上网方式(0:未知,1:Wifi,2:2G,3:3G,4:4G)
		HashMap<String,String> modifiableDeviceNetMap = new HashMap<String,String>();
		modifiableDeviceNetMap.put("0","0");
		modifiableDeviceNetMap.put("1","1");
		modifiableDeviceNetMap.put("2","2");
		modifiableDeviceNetMap.put("3","3");
		modifiableDeviceNetMap.put("4","4");
		//终端运营商,0:移动,1:联通(网通),2:电信 3:其他(如铁通、教育网,政府网,国外等)
		HashMap<String,String> modifiableCarrierMap = new HashMap<String,String>();
		modifiableCarrierMap.put("0","1");
		modifiableCarrierMap.put("1","2");
		modifiableCarrierMap.put("2","3");
		modifiableCarrierMap.put("3","0");
		adtypeMap = Collections.unmodifiableMap(modifiableAdtypeMap);
		deviceIdMap = Collections.unmodifiableMap(modifiableDeviceIdMap);
		deviceNetMap = Collections.unmodifiableMap(modifiableDeviceNetMap);
		carrierMap = Collections.unmodifiableMap(modifiableCarrierMap);
		
		Properties props = new Properties();
		HashMap<String,MdAdspace> modifiableAdspaceMap = new HashMap<String,MdAdspace>();
		try {
			//props.load(MdMapper.class.getClassLoader().getResourceAsStream("md.properties"));
			String scodeurl= MdMapper.class.getProtectionDomain().getCodeSource().getLocation().getPath();
					///app/ad_game-1.0/lib/ad_game.ad_game-1.0.jar
			String purl = scodeurl.substring(0,scodeurl.lastIndexOf(File.separator))
					+File.separator+".."
					+File.separator+"conf/md.properties";;
			System.out.println(purl);
			props.load(new FileInputStream(purl));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Enumeration<?> en = props.propertyNames();
		while(en.hasMoreElements()){
			ArrayList<MdAdScale> modifiableAdscaleList = new ArrayList<MdAdScale>();
			MdAdspace mas = new MdAdspace();
			String key = (String) en.nextElement();
            String property = props.getProperty (key);
            String[] properties = property.split(",");
            String[] adspaceIds = properties[0].split(":");
			mas.bid=adspaceIds[0];
			mas.adsapceId=adspaceIds[1];
			mas.price=Float.parseFloat(properties[1]);
			String[] adscales = properties[2].split("&");
			for (String string : adscales) {
				String[] adscaleArr = string.split(":");
				float width = Float.parseFloat(adscaleArr[0]);
				float height = Float.parseFloat(adscaleArr[1]);
				modifiableAdscaleList.add(new MdAdScale(width/height,adscaleArr[0],adscaleArr[1],width,height));
			}
			Collections.sort(modifiableAdscaleList,new Comparator<MdAdScale>() {

				@Override
				public int compare(MdAdScale o1, MdAdScale o2) {
					// TODO Auto-generated method stub
					float v1 = o1.scale;
					float v2 = o2.scale;
					return v1==v2?0:(v1>v2?1:-1);
				}
			});
			mas.appName = properties[3];
			mas.pkgName = properties[4];
            mas.scales = Collections.unmodifiableList(modifiableAdscaleList);
            modifiableAdspaceMap.put(mas.bid,mas);
		}
		adspaceMap = Collections.unmodifiableMap(modifiableAdspaceMap);
		System.out.println(Json.toJson(modifiableAdspaceMap));
		
	}
	
	public class MdAdspace{
		private String bid;
		private String adsapceId;
		private float price;
		private String appName;
		private String pkgName;
		private List<MdAdScale> scales;
		public String getBid() {
			return bid;
		}
		public String getAdsapceId() {
			return adsapceId;
		}
		public float getPrice() {
			return price;
		}
		public List<MdAdScale> getScales() {
			return scales;
		}
		public String getAppName() {
			return appName;
		}
		public String getPkgName() {
			return pkgName;
		}
		
	}
	public class MdAdScale{
		private float scale;
		private String width;
		private String height;
		private float fheight;
		private float fwidth;
		
		public MdAdScale(float scale, String width, String height,float fwidth,float fheight) {
			super();
			this.scale = scale;
			this.width = width;
			this.height = height;
			this.fwidth = fwidth;
			this.fheight = fheight;
		}
		
		public float getFheight() {
			return fheight;
		}

		public float getFwidth() {
			return fwidth;
		}

		

		public float getScale() {
			return scale;
		}

		public String getWidth() {
			return width;
		}

		public String getHeight() {
			return height;
		}
		
		
		
		
	}
	
	public static MdMapper getInstance() {
		
		return instance;
	}
	public static void main(String[] args) {
		MdMapper.getInstance();
	}
}
