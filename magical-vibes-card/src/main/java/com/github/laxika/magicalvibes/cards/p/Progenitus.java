package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromEverythingEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleIntoLibraryReplacementEffect;

@CardRegistration(set = "CON", collectorNumber = "121")
public class Progenitus extends Card {

    public Progenitus() {
        // Protection from everything.
        addEffect(EffectSlot.STATIC, new ProtectionFromEverythingEffect());
        // If Progenitus would be put into a graveyard from anywhere, reveal it and shuffle it
        // into its owner's library instead.
        addEffect(EffectSlot.STATIC, new ShuffleIntoLibraryReplacementEffect());
    }
}
