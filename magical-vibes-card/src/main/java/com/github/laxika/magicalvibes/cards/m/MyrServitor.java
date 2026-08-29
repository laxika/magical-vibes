package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.SourceIsOnBattlefield;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerReturnsCardsFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;

@CardRegistration(set = "5DN", collectorNumber = "139")
public class MyrServitor extends Card {

    public MyrServitor() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new ConditionalEffect(
                        new SourceIsOnBattlefield(),
                        new EachPlayerReturnsCardsFromGraveyardToBattlefieldEffect(
                                Integer.MAX_VALUE, new CardNamedPredicate("Myr Servitor"))));
    }
}
