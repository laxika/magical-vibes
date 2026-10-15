package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EnergyCountersEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.condition.ControllerEnergyAtLeast;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "PIP", collectorNumber = "38")
@CardRegistration(set = "PIP", collectorNumber = "566")
public class RobobrainWarMind extends Card {

    public RobobrainWarMind() {
        addEffect(EffectSlot.STATIC, new SetPowerToughnessToAmountEffect(
                new CardsInHand(CountScope.CONTROLLER), new Fixed(5)));

        PermanentCount artifactCreaturesYouControl = new PermanentCount(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsArtifactPredicate(),
                        new PermanentIsCreaturePredicate()
                )),
                CountScope.CONTROLLER
        );
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new EnergyCountersEffect(artifactCreaturesYouControl));

        addEffect(EffectSlot.ON_ATTACK, new MayEffect(
                ConditionalEffect.unless(new ControllerEnergyAtLeast(3),
                        SequenceEffect.of(
                                new EnergyCountersEffect(-3),
                                new DrawCardEffect(1)
                        )),
                "Pay {E}{E}{E} to draw a card?"
        ));
    }
}
