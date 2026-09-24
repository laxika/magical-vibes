package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPermanentControllerDrawsCardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetSpellControllerDrawsCardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryHasTargetPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "DMU", collectorNumber = "199")
public class ErtaiResurrected extends Card {

    public ErtaiResurrected() {
        var creatureOrPlaneswalker = new PermanentAnyOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentIsPlaneswalkerPredicate()));
        var anotherCreatureOrPlaneswalker = new PermanentAllOfPredicate(List.of(
                creatureOrPlaneswalker,
                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())));

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Counter target spell, activated ability, or triggered ability. Its controller draws a card",
                        List.of(new TargetSpellControllerDrawsCardEffect(), new CounterSpellEffect()),
                        new StackEntryPredicateTargetFilter(
                                new StackEntryHasTargetPredicate(),
                                "Target must be a spell, activated ability, or triggered ability.")),
                new ChooseOneEffect.ChooseOneOption(
                        "Destroy another target creature or planeswalker. Its controller draws a card",
                        List.of(new TargetPermanentControllerDrawsCardEffect(),
                                new DestroyTargetPermanentEffect(anotherCreatureOrPlaneswalker)),
                        new PermanentPredicateTargetFilter(
                                anotherCreatureOrPlaneswalker,
                                "Target must be another creature or planeswalker."))
        ), true, 0, 1));
    }
}
