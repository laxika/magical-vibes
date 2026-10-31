package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.ReturnCardsFromControllerGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "MOM", collectorNumber = "128")
public class UnsealTheNecropolis extends Card {

    public UnsealTheNecropolis() {
        addSpellEffect(new MillEffect(3, MillRecipient.CONTROLLER));
        addSpellEffect(new MillEffect(3, MillRecipient.EACH_OPPONENT));
        addSpellEffect(new ReturnCardsFromControllerGraveyardToHandEffect(
                new CardTypePredicate(CardType.CREATURE), new Fixed(2)));
    }
}
