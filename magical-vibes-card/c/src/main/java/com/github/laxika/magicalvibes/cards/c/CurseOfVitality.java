package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.AttackingPlayerIsOpponent;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.AttacksEnchantedPlayer;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "C17", collectorNumber = "3")
public class CurseOfVitality extends Card {

    public CurseOfVitality() {
        addTrigger(Triggers.playerAttacks(),
                TriggerOptions.DEFAULT.withInterveningIf(new AttacksEnchantedPlayer()),
                SequenceEffect.of(
                        new GainLifeEffect(2),
                        new ConditionalEffect(
                                new AllOf(java.util.List.of(
                                        new AttackingPlayerIsOpponent(), new AttacksEnchantedPlayer())),
                                new GainLifeEffect(new Fixed(2), GainLifeRecipient.TRIGGERING_PLAYER))));
    }
}
