package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.condition.AnotherPermanentEnteredThisTurn;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ECL", collectorNumber = "198")
public class ThoughtweftCharge extends Card {

    public ThoughtweftCharge() {
        target(TargetFilters.creature())
                .addSpellEffect(BoostEffect.target(3, 3));
        addSpellEffect(new ConditionalEffect(
                new AnotherPermanentEnteredThisTurn(new CardTypePredicate(CardType.CREATURE)),
                new DrawCardEffect()));
    }
}
