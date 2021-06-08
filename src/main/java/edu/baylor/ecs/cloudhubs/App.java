package edu.baylor.ecs.cloudhubs;

import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;

import java.io.IOException;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws IOException {

        GitHub github = GitHub.connect();

        GHRepository repo = github.createRepository(
                "new-repository","this is my new repository",
                "https://www.kohsuke.org/",true/*public*/);
        repo.addCollaborators(github.getUser("abayer"),github.getUser("rtyler"));
        repo.delete();
    }
}
