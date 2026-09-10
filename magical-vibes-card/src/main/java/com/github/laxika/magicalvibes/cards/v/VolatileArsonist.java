package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.d.DireStrainAnarchist;
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

@CardRegistration(set = "VOW", collectorNumber = "181")
public class VolatileArsonist extends Card {

    public VolatileArsonist() {
        setBackFaceCard(new DireStrainAnarchist());

        target(TargetFilters.creature(), 0, 1)
                .addEffect(EffectSlot.ON_ATTACK, new DealDamageToTargetCreatureEffect(1));
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"
        ), 0, 1).addEffect(EffectSlot.ON_ATTACK,
                new DealDamageToTargetPlayerOrPlaneswalkerEffect(1));
        target(new PermanentPredicateTargetFilter(
                new PermanentIsPlaneswalkerPredicate(),
                "Target must be a planeswalker"
        ), 0, 1).addEffect(EffectSlot.ON_ATTACK,
                new DealDamageToTargetPlayerOrPlaneswalkerEffect(1));
    }

    @Override
    public String getBackFaceClassName() {
        return "DireStrainAnarchist";
    }
}
