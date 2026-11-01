package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SHM", collectorNumber = "159")
@CardRegistration(set = "SLD", collectorNumber = "1578")
public class DireUndercurrents extends Card {

    public DireUndercurrents() {
        // Whenever a blue creature you control enters, you may have target player draw a card.
        // The color gate is applied by TriggeringCardConditionalEffect on the entering creature;
        // the "may" and the (any) player target are chosen at resolution via the MayEffect flow.
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL),
                new TriggeringCardConditionalEffect(
                new CardColorPredicate(CardColor.BLUE),
                new MayEffect(new DrawCardForTargetPlayerEffect(1, false, true),
                        "have target player draw a card")));

        // Whenever a black creature you control enters, you may have target player discard a card.
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL),
                new TriggeringCardConditionalEffect(
                new CardColorPredicate(CardColor.BLACK),
                new MayEffect(new DiscardEffect(1, DiscardRecipient.TARGET_PLAYER),
                        "have target player discard a card")));
    }
}
