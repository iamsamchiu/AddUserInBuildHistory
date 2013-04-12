package org.jenkinsci.plugins;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Cause;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * 此功能主要是取得啟動BUILD的使用者資訊，並將此資訊加入Build Description之中，
 * 讓Build History可以顯示使用者資訊。
 * 
 * AddUserInBuildHistory類別會透過擴充Jenkins Extension：Notifier來達成此一功能，
 * 繼承Notifier的類別將可被使用者加入其Post-build Actions之中，當Build Job啓動時，
 * 無論編譯成功或失敗，Jenkins皆會再呼叫Post-build的所有Actions。
 * 。
 * 
 * @author Sam Chiu
 */
public class AddUserInBuildHistory extends Notifier {

	private static final Logger logger = Logger.getLogger("InsertUserInBuildHistory");

	//測試用變數
	private String testVar = "I am test";
	
    /**
     * 由於Notifier也是一種種Describable物件，此物件會被序列化儲存於XML之中，
     * 並於Jenkins啓動後從XML被反序列回物件，而所有變數無論是否為DataBound，
     * 預設也會被序列化存放。若變數值不希望被儲存XML檔案中，可將變數標示transient。
     * 
     * 例如：
     * private transient boolean enable;
     * 
     */
	private boolean enable;
	
    /**
     * DataBoundConstructor用於標示與config.jelly的UI元件進行綁定，enable變數值會與UI中的
     * checkbox元件連動。
     * 
     */
    @DataBoundConstructor
    public AddUserInBuildHistory(boolean enable) {
    	this.enable = enable;
    }

    /**
     * DataBound的變數需編寫get method，config.jelly將會透過此get method讀取變數值。
     * get method的命名格式需遵循get+變數第一個字母大寫的格式。
     */
    public boolean getEnable() {
        return enable;
    }

    @Override
    public boolean perform(AbstractBuild<?,?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        
    	if(!enable){
    		logger.config("Disable display User in description!!");
    		return true;
    	}
    	
        try {
        	/*
        	 * 透過getCauses方法可以取得啟動該筆Build的來源，若是使用者則會顯示賬號名稱，
        	 * 若是遠端啓動則會顯示用戶端ＩＰ位置
        	 */
            Cause cause = build.getCauses().get(0);
            StringBuffer desc = new StringBuffer(cause.getShortDescription());
            logger.config("Set desc for build:"+desc);
            
            /*
             * 透過setDescription將資訊內容寫入Build Description之中，
             * Description的第一行內容則會顯示於Build History。
             */
            String originalDesc = build.getDescription();
            if (originalDesc !=null && originalDesc.length()>0){
            	desc.append("\n").append(originalDesc);
            }
            
            build.setDescription(desc.toString());
            
        } catch (Exception e) {
        	logger.warning("Can't get CAUSE(User) info.e:"+e.toString());
            return true;
        }
    	
        return true;
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl)super.getDescriptor();
    }
    
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {


        /**
         * 此名稱將顯示於Job Configuration的Post-build Actions設定中.
         */
        @Override
        public String getDisplayName() {
            return "Insert User in Build History";
        }

        @Override
        public boolean isApplicable(Class type) {
            return true;
        }

    }
   
}
