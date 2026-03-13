package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDrawsCardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerShufflesHandAndGraveyardIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;

@CardRegistration(set = "M11", collectorNumber = "75")
public class TimeReversal extends Card {

    public TimeReversal() {
        // Each player shuffles their hand and graveyard into their library, then draws seven cards.
        addEffect(EffectSlot.SPELL, new EachPlayerShufflesHandAndGraveyardIntoLibraryEffect());
        addEffect(EffectSlot.SPELL, new EachPlayerDrawsCardEffect(7));

        // Exile Time Reversal.
        addEffect(EffectSlot.SPELL, new ExileSpellEffect());
    }
}
