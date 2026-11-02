package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.CardTypesAmongPermanentsSacrificedThisTurn;
import com.github.laxika.magicalvibes.model.amount.PermanentTypesAmongCardsInGraveyard;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WOC", collectorNumber = "26")
@CardRegistration(set = "WOC", collectorNumber = "38")
public class KorvoldGleefulGlutton extends Card {

    public KorvoldGleefulGlutton() {
        addStaticEffect(new ReduceOwnCastCostEffect(new CardTypesAmongPermanentsSacrificedThisTurn()));

        PermanentTypesAmongCardsInGraveyard permanentTypes = new PermanentTypesAmongCardsInGraveyard();
        addTrigger(Triggers.dealsCombatDamageToPlayer(), SequenceEffect.of(
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, permanentTypes),
                new DrawCardEffect(permanentTypes)));
    }
}
