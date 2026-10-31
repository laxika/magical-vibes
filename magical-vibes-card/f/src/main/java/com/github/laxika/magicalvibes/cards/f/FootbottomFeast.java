package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetCardsFromGraveyardOnTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "LRW", collectorNumber = "115")
@CardRegistration(set = "CMD", collectorNumber = "84")
public class FootbottomFeast extends Card {

    public FootbottomFeast() {
        addSpellEffect(new PutTargetCardsFromGraveyardOnTopOfLibraryEffect(new CardTypePredicate(CardType.CREATURE)));
        addSpellEffect(new DrawCardEffect());
    }
}
