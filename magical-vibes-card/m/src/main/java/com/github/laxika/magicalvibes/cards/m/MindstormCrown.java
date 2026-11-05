package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControllerHadNoCardsInHandAtTurnStart;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MRD", collectorNumber = "207")
public class MindstormCrown extends Card {

    public MindstormCrown() {
        ControllerHadNoCardsInHandAtTurnStart condition = new ControllerHadNoCardsInHandAtTurnStart();
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), SequenceEffect.of(
                new ConditionalEffect(condition, new DrawCardEffect()),
                new ConditionalEffect(new NotCondition(condition),
                        new DealDamageToPlayersEffect(1, DamageRecipient.CONTROLLER))));
    }
}
