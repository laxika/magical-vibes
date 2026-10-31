package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

public class RestorativeBurst extends Card {

    public RestorativeBurst() {
        addSpellEffect(new ReturnTargetCardsFromGraveyardToHandEffect(
                new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.CREATURE),
                        new CardTypePredicate(CardType.LAND),
                        new CardTypePredicate(CardType.PLANESWALKER))),
                2));
        addSpellEffect(new GainLifeEffect(new Fixed(4), GainLifeRecipient.EACH_PLAYER));
        addSpellEffect(new ExileSpellEffect());
    }
}
