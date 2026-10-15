package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "OTC", collectorNumber = "18")
@CardRegistration(set = "OTC", collectorNumber = "54")
public class BackInTown extends Card {

    public BackInTown() {
        addEffect(EffectSlot.SPELL, new ReturnTargetCardsFromGraveyardToBattlefieldEffect(
                new CardAllOfPredicate(List.of(
                        new CardTypePredicate(CardType.CREATURE),
                        new CardAnyOfPredicate(List.of(
                                new CardSubtypePredicate(CardSubtype.ASSASSIN),
                                new CardSubtypePredicate(CardSubtype.MERCENARY),
                                new CardSubtypePredicate(CardSubtype.PIRATE),
                                new CardSubtypePredicate(CardSubtype.ROGUE),
                                new CardSubtypePredicate(CardSubtype.WARLOCK)
                        ))
                ))));
    }
}
