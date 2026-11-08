package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.TriggeringPermanentPower;
import com.github.laxika.magicalvibes.model.amount.TriggeringPermanentToughness;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "VIS", collectorNumber = "57")
public class DeathWatch extends Card {

    public DeathWatch() {
        // Enchant creature. When enchanted creature dies, its controller loses life equal to its
        // power and you gain life equal to its toughness: one ability, the creature as it last
        // existed on the battlefield.
        target(TargetFilters.creature()).addTrigger(Triggers.dies(ObjectRelation.ENCHANTED),
                SequenceEffect.of(
                        new LoseLifeEffect(new TriggeringPermanentPower(),
                                LoseLifeRecipient.TRIGGERING_PERMANENT_CONTROLLER),
                        new GainLifeEffect(new TriggeringPermanentToughness())));
    }
}
