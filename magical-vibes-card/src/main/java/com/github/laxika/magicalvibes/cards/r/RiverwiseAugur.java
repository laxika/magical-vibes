package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.HandToLibraryPlacement;
import com.github.laxika.magicalvibes.model.effect.DrawThenPutCardsFromHandOnTopOrBottomOfLibraryEffect;

@CardRegistration(set = "RIX", collectorNumber = "48")
public class RiverwiseAugur extends Card {

    public RiverwiseAugur() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new DrawThenPutCardsFromHandOnTopOrBottomOfLibraryEffect(3, 2, HandToLibraryPlacement.TOP));
    }
}
