package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;

@CardRegistration(set = "CHK", collectorNumber = "9")
public class EtherealHaze extends Card {

    public EtherealHaze() {
        addEffect(EffectSlot.SPELL, PreventDamageEffect.allByCreatures());
    }
}
