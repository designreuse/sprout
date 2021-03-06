package net.savantly.sprout.domain.tree.repository;

import java.util.List;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.springframework.beans.factory.annotation.Autowired;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.mongodb.morphia.MorphiaQuery;

import net.savantly.sprout.domain.tree.QTree;
import net.savantly.sprout.domain.tree.Tree;

public class TreeRepositoryImpl implements TreeRepositoryCustom{
	
	@Autowired TreeRepository treeRepository;
	@Autowired Datastore datastore;
	@Autowired Morphia morphia;
	
	TreeSummaryFactory treeSummaryFactory = new TreeSummaryFactory();

	@SuppressWarnings("unchecked")
	@Override
	public List<TreeSummary> findAllTreeSummaryByUsername(String username) {
		Predicate predicate = QTree.tree.createdBy.eq(username);
		MorphiaQuery<Tree> query = new MorphiaQuery<>(morphia, datastore, QTree.tree);
		List<Tree> mQResult = query.where(predicate).fetch();
		List<?> result = Projections.list(treeSummaryFactory).newInstance(mQResult);
		return  (List<TreeSummary>) result;
	}

}
