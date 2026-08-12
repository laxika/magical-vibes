package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutOnBottomOfLibraryInsteadOfDyingEffect;

@CardRegistration(set = "ZEN", collectorNumber = "171")
public class NissasChosen extends Card {

    public NissasChosen() {
        // "If this creature would die, put it on the bottom of its owner's library instead."
        addEffect(EffectSlot.STATIC, new PutOnBottomOfLibraryInsteadOfDyingEffect());
    }
}
