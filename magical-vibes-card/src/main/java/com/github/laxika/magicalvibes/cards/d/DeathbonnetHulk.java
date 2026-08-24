package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileAnyGraveyardCardThenIfMatchesEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

public class DeathbonnetHulk extends Card {

    public DeathbonnetHulk() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new ExileAnyGraveyardCardThenIfMatchesEffect(
                        new CardTruePredicate(),
                        new CardTypePredicate(CardType.CREATURE),
                        new PutCountersOnSourceEffect(1, 1, 1)));
    }
}
