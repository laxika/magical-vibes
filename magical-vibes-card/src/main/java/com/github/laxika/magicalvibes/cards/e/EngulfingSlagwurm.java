package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.EventStat;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.RiderRecipient;

@CardRegistration(set = "SOM", collectorNumber = "118")
public class EngulfingSlagwurm extends Card {

    public EngulfingSlagwurm() {
        // Destroy target creature. You gain life equal to that creature's toughness.
        addEffect(EffectSlot.ON_BLOCK, new DestroyTargetPermanentThenEffect(
                EventStat.TOUGHNESS, new GainLifeEffect(new EventValue()), RiderRecipient.CONTROLLER));
        addEffect(EffectSlot.ON_BECOMES_BLOCKED, new DestroyTargetPermanentThenEffect(
                EventStat.TOUGHNESS, new GainLifeEffect(new EventValue()), RiderRecipient.CONTROLLER),
                TriggerMode.PER_BLOCKER);
    }
}
