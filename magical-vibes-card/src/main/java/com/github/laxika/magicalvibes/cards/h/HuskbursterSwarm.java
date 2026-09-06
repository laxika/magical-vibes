package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInExile;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "BLB", collectorNumber = "98")
public class HuskbursterSwarm extends Card {

    public HuskbursterSwarm() {
        CardTypePredicate creatureCard = new CardTypePredicate(CardType.CREATURE);
        addEffect(EffectSlot.STATIC, new ReduceOwnCastCostEffect(new Sum(
                new CardsInExile(creatureCard, CountScope.CONTROLLER),
                new CardsInGraveyard(creatureCard, CountScope.CONTROLLER))));
    }
}
