package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReorderTopCardsOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleLibraryEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "POR", collectorNumber = "62")
public class Omen extends Card {

    public Omen() {
        addEffect(EffectSlot.SPELL, new ReorderTopCardsOfLibraryEffect(3));
        addEffect(EffectSlot.SPELL, new MayEffect(new ShuffleLibraryEffect(false), "You may shuffle your library."));
        addEffect(EffectSlot.SPELL, new DrawCardEffect(1));
    }
}
