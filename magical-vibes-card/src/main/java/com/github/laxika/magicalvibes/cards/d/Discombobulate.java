package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ReorderTopCardsOfLibraryEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "81")
@CardRegistration(set = "ONS", collectorNumber = "79")
public class Discombobulate extends Card {

    public Discombobulate() {
        addEffect(EffectSlot.SPELL, new CounterSpellEffect());
        addEffect(EffectSlot.SPELL, new ReorderTopCardsOfLibraryEffect(4));
    }
}
