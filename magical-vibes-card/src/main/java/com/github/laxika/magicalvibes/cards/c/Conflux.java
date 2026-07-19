package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForOneCardOfEachColorToHandEffect;

@CardRegistration(set = "CON", collectorNumber = "102")
public class Conflux extends Card {

    public Conflux() {
        addEffect(EffectSlot.SPELL, new SearchLibraryForOneCardOfEachColorToHandEffect());
    }
}
