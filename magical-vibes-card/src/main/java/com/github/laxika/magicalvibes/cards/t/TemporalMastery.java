package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MiracleCast;
import com.github.laxika.magicalvibes.model.effect.ControllerExtraTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;

@CardRegistration(set = "INR", collectorNumber = "90")
public class TemporalMastery extends Card {

    public TemporalMastery() {
        // Miracle {1}{U}
        addCastingOption(new MiracleCast("{1}{U}"));

        // Take an extra turn after this one.
        addEffect(EffectSlot.SPELL, new ControllerExtraTurnEffect(1));

        // Exile Temporal Mastery.
        addEffect(EffectSlot.SPELL, new ExileSpellEffect());
    }
}
