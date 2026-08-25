package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.DescendedThisTurn;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "LCI", collectorNumber = "234")
public class MoltenCollapse extends Card {

    public MoltenCollapse() {
        var creatureOrPlaneswalker = new PermanentPredicateTargetFilter(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsPlaneswalkerPredicate()
                )),
                "Target must be a creature or planeswalker.");
        var noncreatureNonlandManaValueOneOrLess = new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentNotPredicate(new PermanentIsCreaturePredicate()),
                        new PermanentNotPredicate(new PermanentIsLandPredicate()),
                        new PermanentMaxManaValuePredicate(1)
                )),
                "Target must be a noncreature, nonland permanent with mana value 1 or less.");

        addEffect(EffectSlot.SPELL, ChooseOneEffect.oneOrMoreWhen(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Destroy target creature or planeswalker",
                        new DestroyTargetPermanentEffect(), creatureOrPlaneswalker),
                new ChooseOneEffect.ChooseOneOption(
                        "Destroy target noncreature, nonland permanent with mana value 1 or less",
                        new DestroyTargetPermanentEffect(), noncreatureNonlandManaValueOneOrLess)
        ), new DescendedThisTurn()));
    }
}
