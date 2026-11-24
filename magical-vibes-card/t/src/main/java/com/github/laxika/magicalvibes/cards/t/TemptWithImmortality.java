package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerReturnsCardsFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.PlayerSubject;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TemptingOfferEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "C13", collectorNumber = "95")
public class TemptWithImmortality extends Card {

    private static final CardTypePredicate CREATURE = new CardTypePredicate(CardType.CREATURE);

    public TemptWithImmortality() {
        addSpellEffect(returnCreature(Subjects.you()));
        addSpellEffect(TemptingOfferEffect.of(
                "Return a creature card from your graveyard to the battlefield? If you do, its controller returns "
                        + "another creature card from their graveyard.",
                TemptWithImmortality::returnCreature)
                .offeredOnlyTo(Subjects.ownersOf(Subjects.cardsInGraveyards(Subjects.eachOpponent(), CREATURE))));
    }

    /** "[players] return a creature card from their graveyard to the battlefield". */
    private static CardEffect returnCreature(PlayerSubject players) {
        return EachPlayerReturnsCardsFromGraveyardToBattlefieldEffect.mandatory(1, CREATURE).by(players);
    }
}
