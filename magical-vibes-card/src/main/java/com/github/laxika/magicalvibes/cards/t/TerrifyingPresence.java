package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "AVR", collectorNumber = "196")
public class TerrifyingPresence extends Card {

    public TerrifyingPresence() {
        // "Prevent all combat damage that would be dealt by creatures other than target creature this turn."
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, PreventDamageEffect.allCombatExceptTargetCreature());
    }
}
