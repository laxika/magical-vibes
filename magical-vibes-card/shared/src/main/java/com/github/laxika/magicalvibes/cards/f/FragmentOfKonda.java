package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

public class FragmentOfKonda extends Card {

    public FragmentOfKonda() {
        addEffect(EffectSlot.ON_DEATH, new DrawCardEffect(1));
    }
}
