package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "ECL", collectorNumber = "184")
public class MoonVigilAdherents extends Card {

    public MoonVigilAdherents() {
        var creatureCount = new PermanentCount(new PermanentIsCreaturePredicate(), CountScope.CONTROLLER);
        var creatureCardsInGraveyard = new CardsInGraveyard(
                new CardTypePredicate(CardType.CREATURE), CountScope.CONTROLLER);
        var boost = new Sum(creatureCount, creatureCardsInGraveyard);
        addEffect(EffectSlot.STATIC, new BoostSelfEffect(boost, boost));
    }
}
