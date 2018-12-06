package ccp.cibot;

import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;

import java.util.logging.Logger;

public class JobStatusAnalyzer
{
    @FunctionalInterface
    public interface FailureCallback {
        void jobFailed();
    }

    @FunctionalInterface
    public interface UnstableCallback {
        void jobUnstable();
    }

    @FunctionalInterface
    public interface BackToNormalCallback {
        void jobIsBackToNormal();
    }


    private static final Logger LOGGER = Logger.getLogger(JobStatusAnalyzer.class.getName());

    private final FailureCallback failureCallback;
    private final UnstableCallback unstableCallback;
    private final BackToNormalCallback backToNormalCallback;


    public JobStatusAnalyzer(FailureCallback failureCallback, UnstableCallback unstableCallback, BackToNormalCallback backToNormalCallback)
    {
        this.failureCallback = failureCallback;
        this.unstableCallback = unstableCallback;
        this.backToNormalCallback = backToNormalCallback;
    }

    public void analyze(Run<?,?> build, TaskListener listener) throws InterruptedException {

        LOGGER.info("entered analyze, fetching previous build");

        //build failed
        if (build.getResult() == Result.FAILURE) {
            LOGGER.info("job failed, calling callback");


            LOGGER.info("full job name: " + build.getFullDisplayName().toString());
            LOGGER.info("job url: " + build.getUrl());

            LOGGER.info("job summary: " + build.getBuildStatusSummary().message);
            LOGGER.info("job duration: " + build.getDurationString());

            LOGGER.info("job causes: " + build.getCauses().get(0).getShortDescription());


            //LOGGER.info("job log text: " +  build.getLogText().);


            failureCallback.jobFailed();
        }

        //build unstable
        if (build.getResult() == Result.UNSTABLE) {
            LOGGER.info("job unstable, calling callback");
            unstableCallback.jobUnstable();
        }

        //if success send mail only if previous build failed or was unstable
        if (build.getResult() == Result.SUCCESS)
        {
            Result prev = findPreviousBuildResult(build);
            if (prev != null)
            {
                if (prev == Result.FAILURE || prev == Result.UNSTABLE)
                {
                    LOGGER.info("job successful after failure/unstable, calling callback");
                    backToNormalCallback.jobIsBackToNormal();
                } else {
                    LOGGER.info("job successful after nothing/success, do nothing");
                }
            }
        }
    }

    private Result findPreviousBuildResult(Run<?,?> b) throws InterruptedException {
        LOGGER.info("entered findPreviousBuildResult");

        do {
            b = b.getPreviousBuild();
            if (b == null || b.isBuilding()) {
                return null;
            }
        } while((b.getResult()==Result.ABORTED) || (b.getResult()==Result.NOT_BUILT));
        return b.getResult();
    }
}
