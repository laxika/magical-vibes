package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;

@CardRegistration(set = "EVE", collectorNumber = "5")
public class Endure extends Card {

    public Endure() {
        // Prevent all damage that would be dealt to you and permanents you control this turn.
        // The shield keys off each permanent's controller (not its type), so this covers every
        // permanent the controller controls, combat and noncombat alike.
        addEffect(EffectSlot.SPELL, PreventDamageEffect.allToControllerAndCreatures());
    }
}
