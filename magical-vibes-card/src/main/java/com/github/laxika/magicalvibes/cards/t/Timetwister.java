package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDrawsCardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerShufflesZonesIntoLibraryEffect;

@CardRegistration(set = "VMA", collectorNumber = "3")
public class Timetwister extends Card {

    public Timetwister() {
        // Each player shuffles their hand and graveyard into their library, then draws seven cards.
        addEffect(EffectSlot.SPELL, new EachPlayerShufflesZonesIntoLibraryEffect());
        addEffect(EffectSlot.SPELL, new EachPlayerDrawsCardEffect(7));
    }
}
