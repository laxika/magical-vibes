package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.ControllerRadCounters;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayHaveThisLandEnterTappedForRadCountersEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceActivationCostEffect;

import java.util.List;

@CardRegistration(set = "PIP", collectorNumber = "151")
@CardRegistration(set = "PIP", collectorNumber = "443")
@CardRegistration(set = "PIP", collectorNumber = "679")
@CardRegistration(set = "PIP", collectorNumber = "971")
public class MariposaMilitaryBase extends Card {

    public MariposaMilitaryBase() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new MayHaveThisLandEnterTappedForRadCountersEffect(2));

        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{5}",
                List.of(
                        new ReduceActivationCostEffect(new ControllerRadCounters()),
                        new DrawCardEffect(1)),
                "{5}, {T}: Draw a card. This ability costs {1} less to activate for each rad counter you have."
        ));
    }
}
