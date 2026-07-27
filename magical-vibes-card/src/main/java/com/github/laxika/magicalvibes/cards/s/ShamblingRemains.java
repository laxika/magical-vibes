package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;


@CardRegistration(set = "CON", collectorNumber = "124")
public class ShamblingRemains extends Card {

    public ShamblingRemains() {
        // This creature can't block.
        addEffect(EffectSlot.STATIC, new CantBlockEffect());

        // Unearth {B}{R}: Return this card from your graveyard to the battlefield. It gains haste.
        // Exile it at the beginning of the next end step. Unearth only as a sorcery.
        addUnearth("{B}{R}");
    }
}
