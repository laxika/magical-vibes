package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.TriggeringPermanentToughness;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ALA", collectorNumber = "65")
public class BanewaspAffliction extends Card {

    public BanewaspAffliction() {
        // Enchant creature. When enchanted creature dies, that creature's controller loses life equal
        // to its toughness (the creature as it last existed on the battlefield).
        target(TargetFilters.creature()).addTrigger(Triggers.dies(ObjectRelation.ENCHANTED),
                new LoseLifeEffect(new TriggeringPermanentToughness(),
                        LoseLifeRecipient.TRIGGERING_PERMANENT_CONTROLLER));
    }
}
