package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RebirthAnteEffect;

@CardRegistration(set = "4ED", collectorNumber = "267")
public class Rebirth extends Card {

    public Rebirth() {
        // Each player may ante the top card of their library. If a player does, that player's
        // life total becomes 20.
        addEffect(EffectSlot.SPELL, new RebirthAnteEffect());
    }
}
