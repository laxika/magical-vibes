package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetCardsFromGraveyardOnTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "DKA", collectorNumber = "65")
@CardRegistration(set = "DTK", collectorNumber = "104")
public class Gravepurge extends Card {

    public Gravepurge() {
        addSpellEffect(new PutTargetCardsFromGraveyardOnTopOfLibraryEffect(new CardTypePredicate(CardType.CREATURE)));
        addSpellEffect(new DrawCardEffect());
    }
}
