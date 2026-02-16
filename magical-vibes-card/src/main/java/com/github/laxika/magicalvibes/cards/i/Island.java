package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;

public class Island extends Card {

    public Island() {
        addEffect(EffectSlot.ON_TAP, new AwardManaEffect(ManaColor.BLUE));
    }
}
