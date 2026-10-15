package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerHadNoCardsInHandAtTurnStart;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "MRD", collectorNumber = "207")
public class MindstormCrown extends Card {

    public MindstormCrown() {
        ControllerHadNoCardsInHandAtTurnStart condition = new ControllerHadNoCardsInHandAtTurnStart();
        addEffect(EffectSlot.UPKEEP_TRIGGERED, SequenceEffect.of(
                ConditionalEffect.unless(condition, new DrawCardEffect()),
                ConditionalEffect.unless(new NotCondition(condition),
                        new DealDamageToPlayersEffect(1, DamageRecipient.CONTROLLER))));
    }
}
