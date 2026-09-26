package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardThenEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.condition.AttacksEnchantedPlayer;

@CardRegistration(set = "C13", collectorNumber = "105")
public class CurseOfChaos extends Card {

    public CurseOfChaos() {
        addEffect(EffectSlot.ON_ANY_PLAYER_ATTACKS,
                new ConditionalEffect(
                        new AttacksEnchantedPlayer(),
                        new MayEffect(
                                new DiscardCardThenEffect(
                                        null, new DrawCardEffect(), "a card", DiscardRecipient.TARGET_PLAYER),
                                "Discard a card to draw a card?",
                                null,
                                MayChoicePlayer.TARGET_PLAYER)));
    }
}
