package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.ExileAnyNumberOfCardsFromHandCost;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "NEO", collectorNumber = "111")
public class MarchOfWretchedSorrow extends Card {

    public MarchOfWretchedSorrow() {
        addEffect(EffectSlot.SPELL, new ExileAnyNumberOfCardsFromHandCost(
                new CardColorPredicate(CardColor.BLACK), 2));
        target(new PermanentPredicateTargetFilter(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsPlaneswalkerPredicate())),
                "Target must be a creature or planeswalker"))
                .addEffect(EffectSlot.SPELL,
                        new DealDamageToTargetCreatureOrPlaneswalkerEffect(new XValue()));
        addEffect(EffectSlot.SPELL, new GainLifeEffect(new XValue()));
    }
}
