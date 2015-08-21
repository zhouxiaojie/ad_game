package entity;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MdResp implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String adspaceid;
	private int returncode;
	private String cid;
	private int adwidth;
	private int adheight;
	private int adtype;
	private String imgurl;
	private String clickurl;
	private String[] imgtracking;
	private String[] thclkurl;
	
	public String getAdspaceid() {
		return adspaceid;
	}
	public void setAdspaceid(String adspaceid) {
		this.adspaceid = adspaceid;
	}
	public int getReturncode() {
		return returncode;
	}
	public void setReturncode(int returncode) {
		this.returncode = returncode;
	}
	public String getCid() {
		return cid;
	}
	public void setCid(String cid) {
		this.cid = cid;
	}
	public int getAdwidth() {
		return adwidth;
	}
	public void setAdwidth(int adwidth) {
		this.adwidth = adwidth;
	}
	public int getAdheight() {
		return adheight;
	}
	public void setAdheight(int adheight) {
		this.adheight = adheight;
	}
	public int getAdtype() {
		return adtype;
	}
	public void setAdtype(int adtype) {
		this.adtype = adtype;
	}
	public String getImgurl() {
		return imgurl;
	}
	public void setImgurl(String imgurl) {
		this.imgurl = imgurl;
	}
	public String getClickurl() {
		return clickurl;
	}
	public void setClickurl(String clickurl) {
		this.clickurl = clickurl;
	}
	public String[] getImgtracking() {
		return imgtracking;
	}
	public void setImgtracking(String[] imgtracking) {
		this.imgtracking = imgtracking;
	}
	public String[] getThclkurl() {
		return thclkurl;
	}
	public void setThclkurl(String[] thclkurl) {
		this.thclkurl = thclkurl;
	}
	
	
}
