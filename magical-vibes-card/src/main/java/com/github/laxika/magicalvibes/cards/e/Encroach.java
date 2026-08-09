package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseCardsFromTargetHandEffect;
import com.github.laxika.magicalvibes.model.effect.HandChoiceDestination;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "UDS", collectorNumber = "59")
public class Encroach extends Card {

    public Encroach() {
        // Target player reveals their hand. You choose a nonbasic land card from it. That player
        // discards that card.
        addEffect(EffectSlot.SPELL, new ChooseCardsFromTargetHandEffect(
                1,
                List.of(),
                new CardAllOfPredicate(List.of(
                        new CardTypePredicate(CardType.LAND),
                        new CardNotPredicate(CardPredicateUtils.basicLand())
                )),
                HandChoiceDestination.DISCARD
        ));
    }
}
