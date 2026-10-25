package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

public class DireStrainAnarchist extends Card {

    public DireStrainAnarchist() {
        DealDamageToTargetCreatureEffect creatureDamage = new DealDamageToTargetCreatureEffect(2);
        DealDamageToTargetPlayerOrPlaneswalkerEffect playerDamage =
                new DealDamageToTargetPlayerOrPlaneswalkerEffect(2);
        DealDamageToTargetPlayerOrPlaneswalkerEffect planeswalkerDamage =
                new DealDamageToTargetPlayerOrPlaneswalkerEffect(2);
        target(TargetFilters.creature(), 0, 1).bindEffect(creatureDamage);
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"
        ), 0, 1).bindEffect(playerDamage);
        target(new PermanentPredicateTargetFilter(
                new PermanentIsPlaneswalkerPredicate(),
                "Target must be a planeswalker"
        ), 0, 1).bindEffect(planeswalkerDamage);
        addSimultaneousEffects(EffectSlot.ON_ATTACK, creatureDamage, playerDamage, planeswalkerDamage);
        setAllowSharedTargets(true);
    }
}
