package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.ExileAnyGraveyardCardThenIfMatchesEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

public class DeathbonnetHulk extends Card {

    public DeathbonnetHulk() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                new ExileAnyGraveyardCardThenIfMatchesEffect(
                        new CardTruePredicate(),
                        new CardTypePredicate(CardType.CREATURE),
                        new PutCountersOnSourceEffect(1, 1, 1)));
    }
}
