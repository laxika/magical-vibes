package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.condition.MinimumAttackers;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DOM", collectorNumber = "150")
public class WarcryPhoenix extends Card {

    public WarcryPhoenix() {
        addTrigger(new TriggerCondition(Triggers.youAttack(), AbilityZones.GRAVEYARD),
                TriggerOptions.DEFAULT.withEventCondition(new MinimumAttackers(3)),
                new MayPayManaEffect("{2}{R}",
                                ReturnCardFromGraveyardEffect.builder()
                                        .destination(GraveyardChoiceDestination.BATTLEFIELD)
                                        .filter(new CardIsSelfPredicate())
                                        .returnAll(true)
                                        .enterTapped(true)
                                        .enterAttacking(true)
                                        .build(),
                                "Pay {2}{R} to return Warcry Phoenix from your graveyard to the battlefield tapped and attacking?"
                        ));
    }
}
