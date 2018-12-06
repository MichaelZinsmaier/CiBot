package ccp.cibot;

import hudson.model.AbstractBuild;

public class MessageGenerator
{
    public String buildFailed(final AbstractBuild<?, ?> build)
    {


        StringBuilder s = new StringBuilder()
                        .append("JOB FAILED")
                        .append(System.getProperty("line.separator"))
                        .append("JOB: " + build.getProject().getFullDisplayName())
                        .append(System.getProperty("line.separator"))
                        .append(build.getProject().getAbsoluteUrl());


        return s.toString();

    }


    public String buildUnstable(final AbstractBuild<?, ?> build)
    {

        build.getProject().getFullDisplayName();


        return "JOB UNSTABLE";
    }

    public String buildSuccess(final AbstractBuild<?, ?> build)
    {


        StringBuilder s = new StringBuilder()
                        .append("JOB BACK TO NORMAL")
                        .append(System.getProperty("line.separator"))
                        .append("JOB: " + build.getProject().getFullDisplayName())
                        .append(System.getProperty("line.separator"))
                        .append(build.getProject().getAbsoluteUrl())
                        .append(System.getProperty("line.separator"))
                        .append("<img class=\"emoticon-icon the-bottom-line\" abbr=\":bottomline\">");


        return s.toString();

    }

}
