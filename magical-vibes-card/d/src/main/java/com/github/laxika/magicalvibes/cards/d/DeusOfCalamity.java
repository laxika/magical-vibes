package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.EventValueAtLeast;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByTriggeringPlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "SHM", collectorNumber = "204")
@CardRegistration(set = "DDL", collectorNumber = "54")
public class DeusOfCalamity extends Card {

    public DeusOfCalamity() {
        // Trample is auto-loaded from Scryfall.
        // Whenever this creature deals 6 or more damage to an opponent, destroy target land that player controls.
        addTrigger(Triggers.dealsDamageToOpponent(),
                TriggerOptions.DEFAULT.withEventCondition(new EventValueAtLeast(6)),
                new DestroyTargetPermanentEffect(new PermanentAllOfPredicate(List.of(
                        new PermanentIsLandPredicate(),
                        new PermanentControlledByTriggeringPlayerPredicate()))));
    }
}
