package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ExileCardsFromHandCastingCost;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.DealDividedDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DivisionMode;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfIfEvokedEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "SPG", collectorNumber = "47")
@CardRegistration(set = "SPG", collectorNumber = "52")
@CardRegistration(set = "MH2", collectorNumber = "126")
@CardRegistration(set = "MH2", collectorNumber = "313")
@CardRegistration(set = "ECC", collectorNumber = "50")
public class Fury extends Card {

    public Fury() {
        PermanentPredicate creatureOrPlaneswalker = new PermanentAnyOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentIsPlaneswalkerPredicate()));

        target(new PermanentPredicateTargetFilter(
                creatureOrPlaneswalker,
                "Target must be a creature or planeswalker"), 0, 4)
                .addTrigger(Triggers.enters(ObjectRelation.THIS), new DealDividedDamageEffect(
                        new Fixed(4), null, DivisionMode.CHOSEN, creatureOrPlaneswalker,
                        4, false, false, true, false, false, true));

        addCastingOption(new AlternateHandCast(List.of(
                new ExileCardsFromHandCastingCost(new CardColorPredicate(CardColor.RED), "red"))));
        addTrigger(Triggers.enters(ObjectRelation.THIS), new SacrificeSelfIfEvokedEffect());
    }
}
