package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsDrawnThisResolution;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.condition.OpponentAttacksWithAtLeastCreatures;
import com.github.laxika.magicalvibes.model.condition.SourceHasChosenMode;
import com.github.laxika.magicalvibes.model.effect.ChooseModeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentDrawsCardEffect;
import com.github.laxika.magicalvibes.model.effect.GiveTargetPlayerRadCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingSourceControllerPredicate;

import java.util.List;

@CardRegistration(set = "PIP", collectorNumber = "39")
@CardRegistration(set = "PIP", collectorNumber = "567")
@CardRegistration(set = "PIP", collectorNumber = "380")
@CardRegistration(set = "PIP", collectorNumber = "908")
public class StruggleForProjectPurity extends Card {

    private static final String BROTHERHOOD = "Brotherhood";
    private static final String ENCLAVE = "Enclave";

    public StruggleForProjectPurity() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ChooseModeOnEnterEffect(List.of(BROTHERHOOD, ENCLAVE)));

        addEffect(EffectSlot.UPKEEP_TRIGGERED, new ConditionalEffect(
                new SourceHasChosenMode(BROTHERHOOD),
                SequenceEffect.of(
                        new EachOpponentDrawsCardEffect(1),
                        new DrawCardEffect(new CardsDrawnThisResolution()))));

        addEffect(EffectSlot.ON_ANY_PLAYER_ATTACKS,
                new ConditionalEffect(
                        new SourceHasChosenMode(ENCLAVE),
                        new ConditionalEffect(
                                new OpponentAttacksWithAtLeastCreatures(1, false),
                                new GiveTargetPlayerRadCountersEffect(new Scaled(
                                        new PermanentCount(
                                                new PermanentIsAttackingSourceControllerPredicate(),
                                                CountScope.ANY_PLAYER),
                                         2)))));
    }
}
