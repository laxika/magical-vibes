package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachPlayerMayPutLandsFromHandToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerShufflesHandAndPermanentsIntoLibraryAndDrawsEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;

/**
 * The Great Aurora: each player shuffles their hand and all permanents they own into their library,
 * then draws that many cards; each player may then put any number of land cards from their hand
 * onto the battlefield; finally the spell exiles itself instead of going to the graveyard.
 */
@CardRegistration(set = "ORI", collectorNumber = "179")
public class TheGreatAurora extends Card {

    public TheGreatAurora() {
        addEffect(EffectSlot.SPELL, new EachPlayerShufflesHandAndPermanentsIntoLibraryAndDrawsEffect());
        addEffect(EffectSlot.SPELL, new EachPlayerMayPutLandsFromHandToBattlefieldEffect());
        addEffect(EffectSlot.SPELL, new ExileSpellEffect());
    }
}
