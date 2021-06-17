//Noah Lambaria
package edu.baylor.ecs.cloudhubs;

import org.apache.commons.lang3.ObjectUtils;
import org.kohsuke.github.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.*;


public class App 
{
    public static void main( String[] args ) throws IOException {

        //gets token from ~/.github file
        GitHub github = GitHubBuilder.fromPropertyFile().build();

        String org_name = "kubernetes", repo_name = "testing";

        //Initialize the folders for org & repo
        File org = new File("/"+ org_name);
        org.mkdir();
        File repo = new File(org_name + "/" + repo_name);
        repo.mkdirs();


        ArrayList<String> List_of_allowed_labels= new ArrayList<>(Arrays.asList("bugs","bug","need_fix"));
        ArrayList<String> List_of_disallowed_labels= new ArrayList<>(Arrays.asList("Documentation","enhancement"));

        //The commented out code is for organizations, and the second line is for regular repositories
        //List<GHIssue> allIssues = github.getOrganization(org_name).getRepository(repo_name).getIssues(GHIssueState.ALL);
        List<GHIssue> allIssues = github.getRepository("Richard-Hutch/BearMarket").getIssues(GHIssueState.ALL);

        List<GHIssue> positiveIssues = new ArrayList<>();
        List<GHIssue> negativeIssues = new ArrayList<>();
        boolean positive = false, badLabel = false;


        for(GHIssue i: allIssues){
            positive = false;
            badLabel = false;
            //Iterate through all labels for every issue.
            for(GHLabel x : i.getLabels()){
                if(List_of_allowed_labels.contains(x.getName())){ positive = true; }
                if(List_of_disallowed_labels.contains(x.getName())){ badLabel = true; }
                //System.out.println(x.getName());
            }

            if(positive && !badLabel){
                positiveIssues.add(i);
                //System.out.println(i.getTitle());
            }

            //System.out.println("=======");
            //System.out.println(i.getLabels());
        }



        File actualFile = new File(repo, "positive_issues.csv");
        DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(actualFile)));



        for(GHIssue i : positiveIssues){
            //Name & description
            System.out.println(i.getTitle());
            out.write(i.getTitle().getBytes(StandardCharsets.UTF_8));
            out.write(",".getBytes(StandardCharsets.UTF_8));

        }



        out.close();




        //take organization and repository
        //iterate through
        //go through labels
        //if kind/bug
        //skip ones with documentation and kind/bug

        //ignore documentation while going through NeedsFix

        //list of labels and list of non labels

        //only take "Needs fix label", skip the needs fixed labels that have documentation



        //create a folder called organization
        //create a folder called reponame
        //create a file


    }
}
