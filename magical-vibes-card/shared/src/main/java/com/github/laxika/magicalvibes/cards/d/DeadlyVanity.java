package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChooseCreatureOrPlaneswalkerDestroyRestEffect;

public class DeadlyVanity extends Card {

    public DeadlyVanity() {
        addSpellEffect(new ChooseCreatureOrPlaneswalkerDestroyRestEffect());
    }
}
