package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.ChooseModeNotYetChosenEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.FlickerEffect;
import com.github.laxika.magicalvibes.model.effect.FlickerScope;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTiming;

import java.util.List;

@CardRegistration(set = "MSC", collectorNumber = "109")
@CardRegistration(set = "MSC", collectorNumber = "440")
public class KimoyoBeads extends Card {

    public KimoyoBeads() {
        // At the beginning of your end step, choose one that hasn't been chosen. Each mode is
        // consumed by this permanent, and the Prime Bead mode returns it as a new object.
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, new ChooseModeNotYetChosenEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "AV Bead \u2014 Draw a card.",
                        new DrawCardEffect()),
                new ChooseOneEffect.ChooseOneOption(
                        "Communication Bead \u2014 Create two 1/1 white Soldier creature tokens.",
                        CreateTokenEffect.whiteSoldier(2)),
                new ChooseOneEffect.ChooseOneOption(
                        "Prime Bead \u2014 You gain 3 life. Exile this artifact, then return it to the battlefield under its owner's control.",
                        List.of(
                                new GainLifeEffect(3),
                                new FlickerEffect(
                                        FlickerScope.SELF,
                                        null,
                                        ReturnTiming.IMMEDIATE,
                                        TurnStep.END_STEP,
                                        false,
                                        null,
                                        null,
                                        0,
                                        false,
                                        false))))));
    }
}
