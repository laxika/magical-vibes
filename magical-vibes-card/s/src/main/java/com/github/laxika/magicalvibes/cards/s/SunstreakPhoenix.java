package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.BecomeDayAsEntersEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MID", collectorNumber = "162")
@CardRegistration(set = "DBL", collectorNumber = "162")
public class SunstreakPhoenix extends Card {

    public SunstreakPhoenix() {
        addStaticEffect(new BecomeDayAsEntersEffect());
        addTrigger(new TriggerCondition(Triggers.dayBecomesNightOrNightBecomesDay(), AbilityZones.GRAVEYARD),
                TriggerOptions.DEFAULT,
                new MayPayManaEffect(
                        "{1}{R}",
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                                .filter(new CardIsSelfPredicate())
                                .returnAll(true)
                                .enterTapped(true)
                                .build(),
                        "Pay {1}{R} to return Sunstreak Phoenix from your graveyard to the battlefield tapped?"));
    }
}
