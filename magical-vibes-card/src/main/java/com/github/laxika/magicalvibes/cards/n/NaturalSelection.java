package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LibraryOwner;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReorderTopCardsOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleLibraryEffect;

@CardRegistration(set = "2ED", collectorNumber = "213")
public class NaturalSelection extends Card {

    public NaturalSelection() {
        addEffect(EffectSlot.SPELL, new ReorderTopCardsOfLibraryEffect(3, LibraryOwner.TARGET_PLAYER));
        addEffect(EffectSlot.SPELL, new MayEffect(new ShuffleLibraryEffect(true), "You may have that player shuffle their library."));
    }
}
