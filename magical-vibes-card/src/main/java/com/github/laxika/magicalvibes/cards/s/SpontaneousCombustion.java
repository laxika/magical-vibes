package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;

/**
 * Spontaneous Combustion — {1}{B}{R} Instant.
 * As an additional cost to cast this spell, sacrifice a creature.
 * Spontaneous Combustion deals 3 damage to each creature.
 */
@CardRegistration(set = "TMP", collectorNumber = "273")
public class SpontaneousCombustion extends Card {

    public SpontaneousCombustion() {
        // As an additional cost to cast this spell, sacrifice a creature.
        addEffect(EffectSlot.SPELL, new SacrificeCreatureCost());
        // Spontaneous Combustion deals 3 damage to each creature.
        addEffect(EffectSlot.SPELL, new MassDamageEffect(3));
    }
}
