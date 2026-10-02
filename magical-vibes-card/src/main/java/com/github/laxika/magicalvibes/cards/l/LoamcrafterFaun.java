package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.DiscardAnyNumberThenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "WOC", collectorNumber = "19")
@CardRegistration(set = "WOC", collectorNumber = "55")
public class LoamcrafterFaun extends Card {

    public LoamcrafterFaun() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                new DiscardAnyNumberThenEffect(
                        new CardTypePredicate(CardType.LAND),
                        new ReturnTargetCardsFromGraveyardToHandEffect(
                                new CardAllOfPredicate(List.of(
                                        new CardIsPermanentPredicate(),
                                        new CardNotPredicate(new CardTypePredicate(CardType.LAND)))),
                                new EventValue()).withDeclaredGraveyardTarget(),
                        "land cards"),
                "Discard one or more land cards?"));
    }
}
