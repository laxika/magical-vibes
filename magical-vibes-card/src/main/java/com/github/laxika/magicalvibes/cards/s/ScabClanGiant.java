package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SourceFightsRandomOpponentCreatureEffect;

@CardRegistration(set = "DGM", collectorNumber = "101")
public class ScabClanGiant extends Card {

    public ScabClanGiant() {
        // When this creature enters, it fights target creature an opponent controls chosen at
        // random. Nothing is chosen by a player, so the card declares no target filter — the
        // effect picks from the opponents' creatures as it resolves.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new SourceFightsRandomOpponentCreatureEffect());
    }
}
