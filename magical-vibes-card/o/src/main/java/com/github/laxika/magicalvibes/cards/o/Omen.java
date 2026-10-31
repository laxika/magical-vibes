package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReorderTopCardsOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleLibraryEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "POR", collectorNumber = "62")
public class Omen extends Card {

    public Omen() {
        addSpellEffect(new ReorderTopCardsOfLibraryEffect(3));
        addSpellEffect(new MayEffect(new ShuffleLibraryEffect(false), "You may shuffle your library."));
        addSpellEffect(new DrawCardEffect(1));
    }
}
