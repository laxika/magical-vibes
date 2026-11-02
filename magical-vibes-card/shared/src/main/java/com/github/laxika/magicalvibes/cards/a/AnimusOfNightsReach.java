package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

public class AnimusOfNightsReach extends Card {

    public AnimusOfNightsReach() {
        addTrigger(Triggers.attacks(), new BoostSelfEffect(
                new CardsInGraveyard(new CardTypePredicate(CardType.CREATURE), CountScope.DEFENDING_PLAYER),
                new Fixed(0)));
    }
}
