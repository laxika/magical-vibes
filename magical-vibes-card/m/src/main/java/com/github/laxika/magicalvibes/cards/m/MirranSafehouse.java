package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.GainActivatedAbilitiesOfExiledCardsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "ONE", collectorNumber = "232")
public class MirranSafehouse extends Card {

    public MirranSafehouse() {
        addStaticEffect(new GainActivatedAbilitiesOfExiledCardsEffect(
                Subjects.cardsInGraveyards(Subjects.eachPlayer(), new CardTypePredicate(CardType.LAND))));
    }
}
