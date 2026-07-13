package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LibraryOfLatNamEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "6ED", collectorNumber = "78")
public class LibraryOfLatNam extends Card {

    public LibraryOfLatNam() {
        addEffect(EffectSlot.SPELL, new LibraryOfLatNamEffect());
    }
}
