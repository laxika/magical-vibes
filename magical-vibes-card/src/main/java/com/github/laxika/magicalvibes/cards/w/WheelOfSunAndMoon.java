package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RevealAndPutOnBottomOfLibraryInsteadOfGraveyardEffect;

@CardRegistration(set = "SHM", collectorNumber = "243")
public class WheelOfSunAndMoon extends Card {

    public WheelOfSunAndMoon() {
        // Enchant player (non-Curse aura, so mark it explicitly).
        setEnchantPlayer(true);
        addEffect(EffectSlot.STATIC, new RevealAndPutOnBottomOfLibraryInsteadOfGraveyardEffect());
    }
}
