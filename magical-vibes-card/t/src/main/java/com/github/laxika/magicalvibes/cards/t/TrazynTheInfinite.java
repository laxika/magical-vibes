package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.GainActivatedAbilitiesOfExiledCardsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "40K", collectorNumber = "65")
public class TrazynTheInfinite extends Card {

    public TrazynTheInfinite() {
        addStaticEffect(new GainActivatedAbilitiesOfExiledCardsEffect(
                Subjects.cardsInYourGraveyard(new CardTypePredicate(CardType.ARTIFACT))));
    }
}
