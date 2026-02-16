package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReorderTopCardsOfLibraryEffect;

public class SageOwl extends Card {

    public SageOwl() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ReorderTopCardsOfLibraryEffect(4));
    }
}
