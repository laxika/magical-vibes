package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.MadnessCast;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;

@CardRegistration(set = "SOI", collectorNumber = "74")
public class NaggingThoughts extends Card {

    public NaggingThoughts() {
        addSpellEffect(LookAtTopCardsEffect.chooseExactlyNToHandRestToGraveyard(2, 1));
        addCastingOption(new MadnessCast("{1}{U}"));
    }
}
