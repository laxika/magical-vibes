package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "UDS", collectorNumber = "7")
public class FendOff extends Card {

    public FendOff() {
        // Prevent all combat damage that would be dealt by target creature this turn.
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, PreventDamageEffect.allCombatByTargetCreatures());

        // Cycling {2} ({2}, Discard this card: Draw a card.)
        addCycling("{2}");
    }
}
