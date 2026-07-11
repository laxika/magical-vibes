package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SacrificeAnyNumberOfLandsAndSearchThatManyLandsToBattlefieldTappedEffect;

@CardRegistration(set = "MOR", collectorNumber = "136")
public class Scapeshift extends Card {

    public Scapeshift() {
        // Sacrifice any number of lands. Search your library for up to that many land cards,
        // put them onto the battlefield tapped, then shuffle.
        addEffect(EffectSlot.SPELL,
                new SacrificeAnyNumberOfLandsAndSearchThatManyLandsToBattlefieldTappedEffect());
    }
}
