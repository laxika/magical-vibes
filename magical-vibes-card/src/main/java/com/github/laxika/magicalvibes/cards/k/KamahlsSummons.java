package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerRevealsAnyNumberOfCardsFromHandThenCreatesTokensEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ONS", collectorNumber = "269")
public class KamahlsSummons extends Card {

    public KamahlsSummons() {
        addEffect(EffectSlot.SPELL, new EachPlayerRevealsAnyNumberOfCardsFromHandThenCreatesTokensEffect(
                new CardTypePredicate(CardType.CREATURE),
                new CreateTokenEffect("Bear", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR),
                        Set.of(), Set.of())));
    }
}
