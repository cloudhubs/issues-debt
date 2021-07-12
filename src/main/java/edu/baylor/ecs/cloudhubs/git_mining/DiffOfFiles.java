//package edu.baylor.ecs.cloudhubs.git_mining;
//
///**
// * author: Abdullah Al Maruf
// * date: 6/25/21
// * time: 1:53 AM
// * website : https://maruftuhin.com
// */
//
//public class DiffOfFiles {
//    public static void main(String []args){
//        OutputStream out = new ByteArrayOutputStream();
//        diff = git.diff().setOutputStream(out)
//                .setOldTree(getTreeIterator("HEAD^^"))
//                .setNewTree(getTreeIterator("HEAD^"));
//    }
//    private AbstractTreeIterator getTreeIterator(String name)
//            throws IOException {
//        final ObjectId id = db.resolve(name);
//        if (id == null)
//            throw new IllegalArgumentException(name);
//        final CanonicalTreeParser p = new CanonicalTreeParser();
//        try (ObjectReader or = db.newObjectReader();
//             RevWalk rw = new RevWalk(db)) {
//            p.reset(or, rw.parseTree(id));
//            return p;
//        }
//    }
//}
