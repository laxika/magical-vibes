package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachOtherPlayerMayDrawUpToNCardsEffect;

@CardRegistration(set = "MMQ", collectorNumber = "85")
public class IndenturedDjinn extends Card {

    public IndenturedDjinn() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new EachOtherPlayerMayDrawUpToNCardsEffect(3));
    }
}
