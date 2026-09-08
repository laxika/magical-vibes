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
        target(TargetFilters.creature(), 0, 1)
                .addEffect(EffectSlot.ON_ATTACK, new DealDamageToTargetCreatureEffect(2));
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"
        ), 0, 1).addEffect(EffectSlot.ON_ATTACK,
                new DealDamageToTargetPlayerOrPlaneswalkerEffect(2));
        target(new PermanentPredicateTargetFilter(
                new PermanentIsPlaneswalkerPredicate(),
                "Target must be a planeswalker"
        ), 0, 1).addEffect(EffectSlot.ON_ATTACK,
                new DealDamageToTargetPlayerOrPlaneswalkerEffect(2));
    }
}
