package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardsOpponentPaysLifeOrToHandEffect;

@CardRegistration(set = "XLN", collectorNumber = "126")
public class SwordPointDiplomacy extends Card {

    public SwordPointDiplomacy() {
        addSpellEffect(new RevealTopCardsOpponentPaysLifeOrToHandEffect(3, 3));
    }
}
