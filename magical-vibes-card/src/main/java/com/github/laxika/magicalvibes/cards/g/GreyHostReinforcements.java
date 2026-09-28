package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "LTC", collectorNumber = "14")
@CardRegistration(set = "LTC", collectorNumber = "98")
public class GreyHostReinforcements extends Card {

    public GreyHostReinforcements() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ExileGraveyardCardsEffect(
                0,
                GraveyardExileScope.TARGET_PLAYER_ALL_MATCHING,
                null,
                null,
                false,
                false,
                false,
                new CardTypePredicate(CardType.CREATURE),
                false));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, new EventValue()));
    }
}
