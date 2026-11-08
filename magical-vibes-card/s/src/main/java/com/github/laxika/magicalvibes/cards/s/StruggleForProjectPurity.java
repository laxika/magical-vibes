package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CardsDrawnThisResolution;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.OpponentAttacksWithAtLeastCreatures;
import com.github.laxika.magicalvibes.model.condition.SourceHasChosenMode;
import com.github.laxika.magicalvibes.model.effect.ChooseModeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentDrawsCardEffect;
import com.github.laxika.magicalvibes.model.effect.GiveTargetPlayerRadCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "PIP", collectorNumber = "39")
@CardRegistration(set = "PIP", collectorNumber = "567")
@CardRegistration(set = "PIP", collectorNumber = "380")
@CardRegistration(set = "PIP", collectorNumber = "908")
public class StruggleForProjectPurity extends Card {

    private static final String BROTHERHOOD = "Brotherhood";
    private static final String ENCLAVE = "Enclave";

    public StruggleForProjectPurity() {
        addStaticEffect(new ChooseModeOnEnterEffect(List.of(BROTHERHOOD, ENCLAVE)));

        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withEventCondition(new SourceHasChosenMode(BROTHERHOOD)),
                SequenceEffect.of(
                        new EachOpponentDrawsCardEffect(1),
                        new DrawCardEffect(new CardsDrawnThisResolution())));

        // "Whenever a player attacks you with one or more creatures" is the trigger event itself, so the
        // attack is judged once when it is declared. Rechecking it on resolution would wrongly stop the
        // trigger when every declared attacker has since left combat.
        addTrigger(Triggers.playerAttacks(),
                TriggerOptions.DEFAULT.withEventCondition(
                        new AllOf(List.of(
                                new SourceHasChosenMode(ENCLAVE),
                                new OpponentAttacksWithAtLeastCreatures(1, false)))),
                        new GiveTargetPlayerRadCountersEffect(new Scaled(new XValue(), 2)));
    }
}
