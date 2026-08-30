package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ShuffleTargetCardFromExileIntoOwnersLibraryEffect;

@CardRegistration(set = "FUT", collectorNumber = "136")
public class Riftsweeper extends Card {

    public Riftsweeper() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ShuffleTargetCardFromExileIntoOwnersLibraryEffect());
    }
}
