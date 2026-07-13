package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutOnTopOfLibraryInsteadOfDyingEffect;

@CardRegistration(set = "6ED", collectorNumber = "133")
public class GravebaneZombie extends Card {

    public GravebaneZombie() {
        // "If Gravebane Zombie would die, put it on top of its owner's library instead."
        addEffect(EffectSlot.STATIC, new PutOnTopOfLibraryInsteadOfDyingEffect());
    }
}
