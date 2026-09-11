package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

public class AnimusOfNightsReach extends Card {

    public AnimusOfNightsReach() {
        addEffect(EffectSlot.ON_ATTACK, new BoostSelfEffect(
                new CardsInGraveyard(new CardTypePredicate(CardType.CREATURE), CountScope.DEFENDING_PLAYER),
                new Fixed(0)));
    }
}
