package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;

@CardRegistration(set = "SHM", collectorNumber = "159")
public class DireUndercurrents extends Card {

    public DireUndercurrents() {
        // Whenever a blue creature you control enters, you may have target player draw a card.
        // The color gate is applied by TriggeringCardConditionalEffect on the entering creature;
        // the "may" and the (any) player target are chosen at resolution via the MayEffect flow.
        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD, new TriggeringCardConditionalEffect(
                new CardColorPredicate(CardColor.BLUE),
                new MayEffect(new DrawCardForTargetPlayerEffect(1, false, true),
                        "have target player draw a card")));

        // Whenever a black creature you control enters, you may have target player discard a card.
        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD, new TriggeringCardConditionalEffect(
                new CardColorPredicate(CardColor.BLACK),
                new MayEffect(new DiscardEffect(1, DiscardRecipient.TARGET_PLAYER),
                        "have target player discard a card")));
    }
}
