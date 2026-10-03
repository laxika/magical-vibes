package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;

/** An attack-group effect that needs the matching attackers bound when the trigger is created. */
public interface AttackingCreaturesAwareEffect extends CardEffect {

    PermanentPredicate attackingCreaturePredicate();

    CardEffect withAttackingCreatures(List<AttackingPermanentSnapshot> attackers);
}
