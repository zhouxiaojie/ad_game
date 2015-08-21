package protocbuf;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import protocbuf.MgxBidReq.MgxBidRequest;

public class Test {
	public static void main(String[] args) throws FileNotFoundException, IOException {
		MgxBidRequest.Builder req = MgxBidRequest.newBuilder();
		req.setVersion(1111);
		req.setBidGuid("12`121`2");
		req.setVisitorId(123);
		req.setIp("192.168.1.1");
		req.setUserAgent("chrome");
		req.addUserSegment("sb");
		req.setSiteId("1321");
		
	    FileOutputStream output = new FileOutputStream("./Test.txt");
		req.build().writeTo(output);
		output.close();
		
	}
}
