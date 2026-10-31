package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;

@CardRegistration(set = "ODY", collectorNumber = "81")
public class Dematerialize extends Card {

    public Dematerialize() {
        addSpellEffect(ReturnToHandEffect.target());
        addCastingOption(new FlashbackCast("{5}{U}{U}"));
    }
}
