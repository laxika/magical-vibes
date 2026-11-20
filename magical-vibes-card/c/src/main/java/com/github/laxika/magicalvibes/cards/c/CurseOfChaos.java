package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.condition.AttacksEnchantedPlayer;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "C13", collectorNumber = "105")
public class CurseOfChaos extends Card {

    public CurseOfChaos() {
        addTrigger(Triggers.playerAttacks(),
                TriggerOptions.DEFAULT.withEventCondition(new AttacksEnchantedPlayer()),
                new MayEffect(
                        SequenceEffect.of(
                                new DiscardEffect(1, Subjects.triggeringPlayer()),
                                new ConditionalEffect(HappenedThisWay.discarded(),
                                        new DrawCardEffect(1, Subjects.triggeringPlayer()))),
                        "Discard a card to draw a card?",
                        null,
                        MayChoicePlayer.TARGET_PLAYER));
    }
}
