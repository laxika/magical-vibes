package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterSpellIfNoColoredManaSpentEffect;

@CardRegistration(set = "MH2", collectorNumber = "242")
public class VoidMirror extends Card {

    public VoidMirror() {
        addEffect(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL,
                new CounterSpellIfNoColoredManaSpentEffect());
    }
}
