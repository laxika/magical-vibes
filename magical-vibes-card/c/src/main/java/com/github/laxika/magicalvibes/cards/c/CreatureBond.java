package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.TriggeringPermanentToughness;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "4ED", collectorNumber = "66")
@CardRegistration(set = "SUM", collectorNumber = "55")
@CardRegistration(set = "3ED", collectorNumber = "55")
@CardRegistration(set = "2ED", collectorNumber = "56")
@CardRegistration(set = "LEA", collectorNumber = "55")
@CardRegistration(set = "LEB", collectorNumber = "56")
public class CreatureBond extends Card {

    public CreatureBond() {
        // Enchant creature. When enchanted creature dies, this Aura deals damage equal to that
        // creature's toughness to the creature's controller (the creature as it last existed on the
        // battlefield).
        target(TargetFilters.creature()).addTrigger(Triggers.dies(ObjectRelation.ENCHANTED),
                new DealDamageToPlayersEffect(new TriggeringPermanentToughness(),
                        DamageRecipient.TRIGGERING_PERMANENT_CONTROLLER));
    }
}
