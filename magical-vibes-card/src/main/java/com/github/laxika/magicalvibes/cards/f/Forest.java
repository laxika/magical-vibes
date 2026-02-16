package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;

public class Forest extends Card {

    public Forest() {
        addEffect(EffectSlot.ON_TAP, new AwardManaEffect(ManaColor.GREEN));
    }
}
