package com.crossover.trial.journals.repository;

import java.util.Collection;

import org.springframework.data.jpa.repository.JpaRepository;

import com.crossover.trial.journals.model.Category;
import com.crossover.trial.journals.model.Subscription;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long>{

	 Collection<Subscription> findByCategory(Category category);
}
