package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardCast;
import com.github.laxika.magicalvibes.model.condition.CardDiscardedThisTurn;
import com.github.laxika.magicalvibes.model.effect.DiscardCardThenEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

import java.util.List;

@CardRegistration(set = "MSC", collectorNumber = "657")
public class GreenGoblinBackForMore extends Card {

    public GreenGoblinBackForMore() {
        addCastingOption(new GraveyardCast(null, "{3}{B}{B}", List.of(), new CardDiscardedThisTurn()));

        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED, new MayEffect(
                new DiscardCardThenEffect(null,
                        new DiscardEffect(1, DiscardRecipient.EACH_OPPONENT), "a card"),
                "Discard a card?"));
    }
}
