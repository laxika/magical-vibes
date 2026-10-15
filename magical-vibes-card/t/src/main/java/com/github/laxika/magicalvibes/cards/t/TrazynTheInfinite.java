package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainActivatedAbilitiesOfCardsInControllerGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "40K", collectorNumber = "65")
public class TrazynTheInfinite extends Card {

    public TrazynTheInfinite() {
        addEffect(EffectSlot.STATIC,
                new GainActivatedAbilitiesOfCardsInControllerGraveyardEffect(
                        new CardTypePredicate(CardType.ARTIFACT)));
    }
}
