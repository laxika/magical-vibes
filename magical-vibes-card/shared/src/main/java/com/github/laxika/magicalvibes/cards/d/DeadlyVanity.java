package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseCreatureOrPlaneswalkerDestroyRestEffect;

public class DeadlyVanity extends Card {

    public DeadlyVanity() {
        addEffect(EffectSlot.SPELL, new ChooseCreatureOrPlaneswalkerDestroyRestEffect());
    }
}
