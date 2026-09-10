package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.b.Bloodletting;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BecomePreparedEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;

/** Leech Collector // Bloodletting (SOS 88). */
@CardRegistration(set = "SOS", collectorNumber = "88")
public class LeechCollectorBloodletting extends Card {

    public LeechCollectorBloodletting() {
        setBackFaceCard(new Bloodletting());

        // Whenever you gain life for the first time each turn, this creature becomes prepared.
        addEffect(EffectSlot.ON_CONTROLLER_GAINS_LIFE,
                new OncePerTurnTriggerEffect(new BecomePreparedEffect()));
    }

    @Override
    public String getBackFaceClassName() {
        return "Bloodletting";
    }
}
