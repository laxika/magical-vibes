package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "PLC", collectorNumber = "32")
@CardRegistration(set = "DDM", collectorNumber = "17")
@CardRegistration(set = "TSR", collectorNumber = "51")
@CardRegistration(set = "C16", collectorNumber = "82")
public class AeonChronicler extends Card {

    public AeonChronicler() {
        CardsInHand cardsInHand = new CardsInHand(CountScope.CONTROLLER);
        addStaticEffect(new SetPowerToughnessToAmountEffect(cardsInHand, cardsInHand));
        addTrigger(Triggers.timeCounterRemovedFromThisInExile(), new DrawCardEffect());
        addActivatedAbility(AbilityZones.HAND, new ActivatedAbility(
                false,
                "{X}{3}{U}",
                List.of(),
                "Suspend X\u2014{X}{3}{U}",
                ActivationTimingRestriction.SORCERY_SPEED
        ).withSuspendsSourceFromHandX());
    }
}
