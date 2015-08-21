import mapper.MdMapper;
import play.Application;
import play.GlobalSettings;
import play.Logger;

public class Global extends GlobalSettings {
	public void onStart(Application app) {
		MdMapper.getInstance();
		Logger.info("Application start...");
    }

    public void onStop(Application app) {
        Logger.info("Application shutdown...");
    }
}