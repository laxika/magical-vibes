package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardThenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "HOB", collectorNumber = "45")
public class LongLakeNuisance extends Card {

    public LongLakeNuisance() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                SequenceEffect.of(
                        new DrawCardEffect(1),
                        new DiscardCardThenEffect(
                                null,
                                CreateTokenEffect.whiteSoldier(1),
                                "a card",
                                new CardNotPredicate(new CardTypePredicate(CardType.LAND)))));
    }
}
