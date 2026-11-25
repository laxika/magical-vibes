package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

public class DireStrainAnarchist extends Card {

    public DireStrainAnarchist() {
        DealDamageEffect creatureDamage = DealDamageEffect.toTargetCreature(2);
        DealDamageEffect playerDamage =
                DealDamageEffect.toTargetPlayerOrPlaneswalker(2);
        DealDamageEffect planeswalkerDamage =
                DealDamageEffect.toTargetPlayerOrPlaneswalker(2);
        target(TargetFilters.creature(), 0, 1).bindEffect(creatureDamage);
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"
        ), 0, 1).bindEffect(playerDamage);
        target(new PermanentPredicateTargetFilter(
                new PermanentIsPlaneswalkerPredicate(),
                "Target must be a planeswalker"
        ), 0, 1).bindEffect(planeswalkerDamage);
        addSimultaneousTrigger(Triggers.attacks(), creatureDamage, playerDamage, planeswalkerDamage);
        setAllowSharedTargets(true);
    }
}
