package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseModeNotYetChosenEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfAndReturnUnderOpponentControlEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "DMU", collectorNumber = "219")
public class SolKanarTheTainted extends Card {

    public SolKanarTheTainted() {
        PermanentPredicate creatureOrPlaneswalker = new PermanentAnyOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentIsPlaneswalkerPredicate()));
        PermanentPredicate anotherCreatureOrPlaneswalker = new PermanentAllOfPredicate(List.of(
                creatureOrPlaneswalker,
                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())));
        PermanentPredicateTargetFilter damageTarget = new PermanentPredicateTargetFilter(
                anotherCreatureOrPlaneswalker,
                "Target must be another creature or planeswalker");

        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, new ChooseModeNotYetChosenEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Draw a card", new DrawCardEffect()),
                new ChooseOneEffect.ChooseOneOption(
                        "Each opponent loses 2 life and you gain 2 life",
                        List.of(new LoseLifeEffect(2, LoseLifeRecipient.EACH_OPPONENT), new GainLifeEffect(2))),
                new ChooseOneEffect.ChooseOneOption(
                        "Sol'Kanar deals 3 damage to up to one other target creature or planeswalker",
                        List.of(new DealDamageToTargetCreatureOrPlaneswalkerEffect(
                                3, anotherCreatureOrPlaneswalker)),
                        damageTarget, null, 0, 1, false, null),
                new ChooseOneEffect.ChooseOneOption(
                        "Exile Sol'Kanar, then return it to the battlefield under an opponent's control",
                        new ExileSelfAndReturnUnderOpponentControlEffect()))));
    }
}
