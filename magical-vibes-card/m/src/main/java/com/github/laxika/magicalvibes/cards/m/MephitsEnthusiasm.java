package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.EventValueAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterOneShotCreatureSpellPerpetualPowerBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "HBG", collectorNumber = "60")
public class MephitsEnthusiasm extends Card {

    public MephitsEnthusiasm() {
        var creatureOrPlaneswalker = new PermanentAnyOfPredicate(List.of(
                new PermanentIsCreaturePredicate(), new PermanentIsPlaneswalkerPredicate()));
        target(new PermanentPredicateTargetFilter(
                creatureOrPlaneswalker, "Target must be a creature or planeswalker"))
                .addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureOrPlaneswalkerEffect(4));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new EventValueAtLeast(1), new RegisterOneShotCreatureSpellPerpetualPowerBoostEffect()));
    }
}
