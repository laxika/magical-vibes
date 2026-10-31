package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.ReturnCardsFromControllerGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "LCI", collectorNumber = "90")
public class AnotherChance extends Card {

    public AnotherChance() {
        addSpellEffect(new MayEffect(new MillEffect(2, MillRecipient.CONTROLLER), "Mill two cards?"));
        addSpellEffect(new ReturnCardsFromControllerGraveyardToHandEffect(
                new CardTypePredicate(CardType.CREATURE), new Fixed(2)));
    }
}
