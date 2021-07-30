package edu.baylor.ecs.cloudhubs.git_mining;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.patch.FileHeader;
import org.eclipse.jgit.patch.HunkHeader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class ChangedFilesBetweenCommit {

    public static void main(String[] args) throws IOException, GitAPIException {
        String repo = "/Users/maruf_maruf1/OneDrive - Baylor University/RA/cloudhubs/tms2020";
        FileRepositoryBuilder repositoryBuilder = new FileRepositoryBuilder();
        Repository repository = repositoryBuilder.setGitDir(new File(repo + "/.git"))
                .readEnvironment() // scan environment GIT_* variables
                .findGitDir() // scan up the file system tree
                .setMustExist(true)
                .build();
        ObjectId oldHead = repository.resolve("c7caed4c8ffe90cc5b777d8e62903288e551d933^^^^{tree}");
        ObjectId head = repository.resolve("c7caed4c8ffe90cc5b777d8e62903288e551d933^^{tree}");

        System.out.println("Printing diff between tree: " + oldHead + " and " + head);

        // prepare the two iterators to compute the diff between
        try (ObjectReader reader = repository.newObjectReader()) {
            CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
            oldTreeIter.reset(reader, oldHead);
            CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
            newTreeIter.reset(reader, head);

            // finally get the list of changed files
            try (Git git = new Git(repository)) {

                Iterable<RevCommit> commits = git.log().all().call();
                int count = 0;
                for (RevCommit commit : commits) {
                    System.out.println("LogCommit: " + commit);
                    count++;
                }
                System.out.println(count);

                List<DiffEntry> diffs = git.diff()
                        .setNewTree(newTreeIter)
                        .setOldTree(oldTreeIter)
                        .call();
                for (DiffEntry entry : diffs) {
                    System.out.println("old: " + entry.getOldId() +
                            ", new: " + entry.getNewPath() +
                            ", entry: " + entry);

                    DiffFormatter diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE);
                    diffFormatter.setRepository(repository);
                    diffFormatter.setContext(0);
                    FileHeader header = diffFormatter.toFileHeader(entry);
                    List<? extends HunkHeader> hunks = header.getHunks();
                    System.out.println(".............. Old path "+ header.getOldPath());
                    System.out.println(".............. New path "+ header.getNewPath());

                    for (HunkHeader hunkHeader : hunks) {
                        System.out.println(".............. "+hunkHeader);
                        for (Edit edit : hunkHeader.toEditList()) {
//                            System.out.println(edit.getBeginA());
                            System.out.println("A: "+edit.getBeginA()+" "+edit.getEndA());
                            System.out.println("B: "+edit.getBeginB()+" "+edit.getEndB());
                        }
                    }


                    // Display the diff
                    System.out.println("Showing diff of " + entry.getNewPath());

                    try (DiffFormatter formatter = new DiffFormatter(System.out)) {
                        formatter.setRepository(repository);
//            formatter.setContext(0);
                        //noinspection ConstantConditions
                        formatter.format(entry);
                    }

//                    ByteArrayOutputStream output = new ByteArrayOutputStream();
//                    DiffFormatter df = new DiffFormatter(output);
//
//                    df.setRepository(git.getRepository());
//                    df.format(entry);
//
//                    Scanner scanner = new Scanner(output.toString("UTF-8"));
//                    int added = 0;
//                    int removed = 0;
//
//                    while (scanner.hasNextLine()) {
//                        String line = scanner.nextLine();
//                        System.out.println(line);
////                        if (line.startsWith("+") && !line.startsWith("+++")) {
////                            System.out.println("+++" + line);
////                        } else if (line.startsWith("-") && !line.startsWith("---")) {
////                            System.out.println("---" + line);
////                        }
//                    }

                }
            }
        }
    }
}