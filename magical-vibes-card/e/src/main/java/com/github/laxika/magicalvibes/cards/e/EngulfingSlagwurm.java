package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.EventStat;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ThenEffectRecipient;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SOM", collectorNumber = "118")
public class EngulfingSlagwurm extends Card {

    public EngulfingSlagwurm() {
        // Destroy target creature. You gain life equal to that creature's toughness.
        addTrigger(Triggers.blocks(), new DestroyTargetPermanentThenEffect(
                EventStat.TOUGHNESS, new GainLifeEffect(new EventValue()), ThenEffectRecipient.CONTROLLER));
        addTrigger(Triggers.becomesBlocked(), TriggerMode.PER_BLOCKER, new DestroyTargetPermanentThenEffect(
                EventStat.TOUGHNESS, new GainLifeEffect(new EventValue()), ThenEffectRecipient.CONTROLLER));
    }
}
