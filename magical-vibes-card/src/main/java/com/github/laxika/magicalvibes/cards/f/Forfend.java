package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventAllDamageToAllCreaturesEffect;

@CardRegistration(set = "MOR", collectorNumber = "10")
public class Forfend extends Card {

    public Forfend() {
        // Prevent all damage that would be dealt to creatures this turn.
        addEffect(EffectSlot.SPELL, new PreventAllDamageToAllCreaturesEffect());
    }
}
