package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "WAR", collectorNumber = "76")
public class AidTheFallen extends Card {

    public AidTheFallen() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Return target creature card from your graveyard to your hand",
                        new ReturnTargetCardsFromGraveyardToHandEffect(
                                new CardTypePredicate(CardType.CREATURE), 1)),
                new ChooseOneEffect.ChooseOneOption(
                        "Return target planeswalker card from your graveyard to your hand",
                        new ReturnTargetCardsFromGraveyardToHandEffect(
                                new CardTypePredicate(CardType.PLANESWALKER), 1)),
                new ChooseOneEffect.ChooseOneOption(
                        "Return target creature card and target planeswalker card from your graveyard to your hand",
                        new ReturnTargetCardsFromGraveyardToHandEffect(
                                new CardAnyOfPredicate(List.of(
                                        new CardTypePredicate(CardType.CREATURE),
                                        new CardTypePredicate(CardType.PLANESWALKER))), 2))
        )));
    }
}
