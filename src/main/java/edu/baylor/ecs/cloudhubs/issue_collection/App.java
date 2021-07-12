//Noah Lambaria
package edu.baylor.ecs.cloudhubs.issue_collection;

import org.apache.commons.lang3.ObjectUtils;
import org.kohsuke.github.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.List;
import java.util.*;


public class App 
{
    public static void main( String[] args ) throws IOException {

        //gets token from ~/.github file
        GitHub github = GitHubBuilder.fromPropertyFile().build();


        String org_name = "zulip", repo_name = "zulip";

        //Initialize the folders for org & repo
        File org = new File("/"+ org_name);
        org.mkdir();
        File repo = new File(org_name + "/" + repo_name);
        repo.mkdirs();


        ArrayList<String> List_of_allowed_labels= new ArrayList<>(Arrays.asList(
                "area: refactoring","area: support","area: testing-coverage","area: testing-infrastructure",
                "bug","difficult","duplicate","has conflicts","rust community request","Issues of interest to the Rust community"
        ));

        ArrayList<String> List_of_disallowed_labels= new ArrayList<>(Arrays.asList(
                "area: documentation (api and integrations)","area: documentation (developer)",
                "area: documentation (production)","area: documentation (user)","area: i18n",
                "area: keyboard UI","area: left-sidebar","area: portico","area: production installer",
                "area: provision","area: settings UI","area: tooling","design","invalid","needs discussion",
                "new feature","A proposed new feature for the product","UX","UX improvements to an existing workflow."
        ));


        //The commented out code is for organizations, and the second line is for regular repositories
        //List<GHIssue> allIssues = github.getOrganization(org_name).getRepository(repo_name).getIssues(GHIssueState.ALL);
        //List<GHIssue> allIssues = github.getRepository("Richard-Hutch/BearMarket").getIssues(GHIssueState.ALL);
        List<GHIssue> allIssues = github.getRepository(org_name + "/" + repo_name).getIssues(GHIssueState.ALL);

        List<GHIssue> positiveIssues = new ArrayList<>();
        List<GHIssue> negativeIssues = new ArrayList<>();
        boolean positive = false, badLabel = false;


        for(GHIssue i: allIssues){
            positive = false;
            badLabel = false;
            //Iterate through all labels for every issue.
            for(GHLabel x : i.getLabels()){
                //skip pull requests
                if(List_of_allowed_labels.contains(x.getName())){ positive = true; }
                if(List_of_disallowed_labels.contains(x.getName())){ badLabel = true; }
            }

                                        //filter out pull requests
            if(positive && !badLabel && !i.isPullRequest()){ positiveIssues.add(i); }
            else{negativeIssues.add(i);}

        }

        File positiveFile = new File(repo, "positive_issues.csv");
        DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(positiveFile)));
        out.writeChars("Title, Pull request, Body, url, labels" + "\n");

        for(GHIssue i : positiveIssues){
            writeLine(i, out);
        }
        out.close();

        File negativeFile = new File(repo, "negative_issues.csv");
        out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(negativeFile)));
        out.writeChars("Title, Pull request, Body, url, labels" + "\n");

        for(GHIssue i : negativeIssues){
            writeLine(i, out);
        }
        out.close();


    }

    private static void writeLine(GHIssue issue, DataOutputStream out) throws IOException {


        //check if title contains comma, and delete it :)
        String theTitle = issue.getTitle().replaceAll(",","");
        out.write(theTitle.getBytes(StandardCharsets.UTF_8));
        out.write(",".getBytes(StandardCharsets.UTF_8));

        String pullReq = String.valueOf(issue.getPullRequest());
        out.write(pullReq.getBytes(StandardCharsets.UTF_8));
        out.write(",".getBytes(StandardCharsets.UTF_8));

        //check if body contains "," so that it will parse correctly
        String theBody = issue.getBody().replaceAll(",","");
        theBody = theBody.replaceAll("[\\t\\n\\r]+"," ");

        out.write(theBody.getBytes(StandardCharsets.UTF_8));
        out.write(",".getBytes(StandardCharsets.UTF_8));
        //out.write(issue.getPullRequest().toString().getBytes(StandardCharsets.UTF_8));
        //out.write(",".getBytes(StandardCharsets.UTF_8));
        out.write(issue.getUrl().toString().getBytes(StandardCharsets.UTF_8));
        out.write(",".getBytes(StandardCharsets.UTF_8));
        getLabels(issue, out);
        out.writeChars("\n");
    }


    private static void getLabels(GHIssue issue, DataOutputStream out) throws IOException {
        for(GHLabel x : issue.getLabels()){
            out.write(x.getName().getBytes(StandardCharsets.UTF_8));
            out.write(";".getBytes(StandardCharsets.UTF_8));
        }
    }
}
