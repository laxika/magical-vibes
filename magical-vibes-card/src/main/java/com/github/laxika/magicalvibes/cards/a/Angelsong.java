package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;


@CardRegistration(set = "ALA", collectorNumber = "4")
public class Angelsong extends Card {

    public Angelsong() {
        // Prevent all combat damage that would be dealt this turn.
        addEffect(EffectSlot.SPELL, PreventDamageEffect.allCombat());

        // Cycling {2} ({2}, Discard this card: Draw a card.) — discard cost is intrinsic.
        addCycling("{2}");
    }
}
