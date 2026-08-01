package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MillAndRegisterDelayedAttackerBoostEffect;

@CardRegistration(set = "VIS", collectorNumber = "94")
public class SongOfBlood extends Card {

    public SongOfBlood() {
        // Mill four cards. Whenever a creature attacks this turn, it gets +1/+0 until end of turn
        // for each creature card put into your graveyard this way.
        addEffect(EffectSlot.SPELL, new MillAndRegisterDelayedAttackerBoostEffect(4, 1, 0));
    }
}
