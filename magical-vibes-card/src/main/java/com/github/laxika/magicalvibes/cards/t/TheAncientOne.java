package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.LastDiscardedCardManaValue;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.CantAttackOrBlockUnlessEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardThenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;

import java.util.List;

@CardRegistration(set = "LCI", collectorNumber = "222")
public class TheAncientOne extends Card {

    public TheAncientOne() {
        addEffect(EffectSlot.STATIC, new CantAttackOrBlockUnlessEffect(
                new GraveyardCardThreshold(8, new CardIsPermanentPredicate()),
                "you have eight or more permanent cards in your graveyard"));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{U}{B}",
                List.of(
                        new DrawCardEffect(1),
                        new DiscardCardThenEffect(
                                null,
                                new MillEffect(new LastDiscardedCardManaValue(), MillRecipient.TARGET_PLAYER),
                                "a card")),
                "{2}{U}{B}: Draw a card, then discard a card. When you discard a card this way, target "
                        + "player mills cards equal to its mana value."));
    }
}
