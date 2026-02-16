package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;

public class Plains extends Card {

    public Plains() {
        addEffect(EffectSlot.ON_TAP, new AwardManaEffect(ManaColor.WHITE));
    }
}
