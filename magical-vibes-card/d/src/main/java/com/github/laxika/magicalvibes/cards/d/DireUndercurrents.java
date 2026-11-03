package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SHM", collectorNumber = "159")
@CardRegistration(set = "SLD", collectorNumber = "1578")
public class DireUndercurrents extends Card {

    public DireUndercurrents() {
        // Whenever a blue creature you control enters, you may have target player draw a card.
        // The color gate is the trigger's filter, judged on the entering creature;
        // the "may" and the (any) player target are chosen at resolution via the MayEffect flow.
        addTrigger(Triggers.enters(
                        new PermanentAllOfPredicate(List.of(
                                Triggers.CREATURE,
                                new PermanentColorInPredicate(Set.of(CardColor.BLUE)))),
                        ObjectRelation.ANOTHER_YOU_CONTROL),
                new MayEffect(new DrawCardForTargetPlayerEffect(1, false, true), "have target player draw a card"));

        // Whenever a black creature you control enters, you may have target player discard a card.
        addTrigger(Triggers.enters(
                        new PermanentAllOfPredicate(List.of(
                                Triggers.CREATURE,
                                new PermanentColorInPredicate(Set.of(CardColor.BLACK)))),
                        ObjectRelation.ANOTHER_YOU_CONTROL),
                new MayEffect(new DiscardEffect(1, DiscardRecipient.TARGET_PLAYER),
                        "have target player discard a card"));
    }
}
