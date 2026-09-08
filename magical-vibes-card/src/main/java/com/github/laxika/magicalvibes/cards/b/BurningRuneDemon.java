package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BurningRuneDemonEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "KHM", collectorNumber = "81")
public class BurningRuneDemon extends Card {

    public BurningRuneDemon() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                new BurningRuneDemonEffect(),
                "Search your library for exactly two cards with different names?"));
    }
}
