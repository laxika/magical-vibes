package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;

@CardRegistration(set = "VOW", collectorNumber = "74")
@CardRegistration(set = "DBL", collectorNumber = "341")
public class ScatteredThoughts extends Card {

    public ScatteredThoughts() {
        addSpellEffect(LookAtTopCardsEffect.chooseExactlyNToHandRestToGraveyard(4, 2));
    }
}
