package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardThenEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTriggeringPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.condition.AttacksEnchantedPlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "C13", collectorNumber = "105")
public class CurseOfChaos extends Card {

    public CurseOfChaos() {
        addTrigger(Triggers.playerAttacks(),
                new ConditionalEffect(
                        new AttacksEnchantedPlayer(),
                        new MayEffect(
                                new DiscardCardThenEffect(
                                        null, new DrawCardForTriggeringPlayerEffect(), "a card", null, false, null, null,
                                        DiscardRecipient.TARGET_PLAYER, true),
                                "Discard a card to draw a card?",
                                null,
                                MayChoicePlayer.TARGET_PLAYER)));
    }
}
