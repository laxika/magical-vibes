package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardOfOwnLibraryEffect;

@CardRegistration(set = "ZEN", collectorNumber = "68")
public class SphinxOfJwarIsle extends Card {

    public SphinxOfJwarIsle() {
        addEffect(EffectSlot.STATIC, new LookAtTopCardOfOwnLibraryEffect());
    }
}
