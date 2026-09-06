package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "ONS", collectorNumber = "225")
public class SearingFlesh extends Card {

    public SearingFlesh() {
        target(new AnyTargetPredicateTargetFilter(
                new PermanentIsPlaneswalkerPredicate(),
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent or planeswalker"
        )).addEffect(EffectSlot.SPELL,
                new DealDamageToTargetPlayerOrPlaneswalkerEffect(7, PlayerRelation.OPPONENT));
    }
}
