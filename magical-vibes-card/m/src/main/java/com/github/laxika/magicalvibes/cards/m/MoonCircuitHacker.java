package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceEnteredBattlefieldThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "NEO", collectorNumber = "67")
public class MoonCircuitHacker extends Card {

    public MoonCircuitHacker() {
        addNinjutsu("{U}");

        addTrigger(Triggers.dealsCombatDamageToPlayer(), new MayEffect(
                SequenceEffect.of(
                        new DrawCardEffect(1),
                        new ConditionalEffect(
                                new NotCondition(new SourceEnteredBattlefieldThisTurn()),
                                new DiscardEffect(1, DiscardRecipient.CONTROLLER))),
                "Draw a card?"));
    }
}
