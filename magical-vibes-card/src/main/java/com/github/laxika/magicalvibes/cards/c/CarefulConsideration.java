package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.CastDuringMainPhase;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "TSP", collectorNumber = "52")
public class CarefulConsideration extends Card {

    public CarefulConsideration() {
        addEffect(EffectSlot.SPELL, new DrawCardEffect(4));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new CastDuringMainPhase(), new DiscardEffect(2, DiscardRecipient.CONTROLLER)));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new NotCondition(new CastDuringMainPhase()), new DiscardEffect(3, DiscardRecipient.CONTROLLER)));
    }
}
