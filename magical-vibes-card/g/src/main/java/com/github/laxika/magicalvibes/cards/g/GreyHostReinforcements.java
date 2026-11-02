package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LTC", collectorNumber = "14")
@CardRegistration(set = "LTC", collectorNumber = "98")
@CardRegistration(set = "LTC", collectorNumber = "416")
public class GreyHostReinforcements extends Card {

    public GreyHostReinforcements() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new ExileGraveyardCardsEffect(
                0,
                GraveyardExileScope.TARGET_PLAYER_ALL_MATCHING,
                null,
                null,
                false,
                false,
                false,
                new CardTypePredicate(CardType.CREATURE),
                false));
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, new EventValue()));
        addTrigger(Triggers.becomesTargetOfOpponentSpellOrAbility(), new CounterUnlessPaysEffect(3));
    }
}
