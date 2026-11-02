package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

/** Back face of Jecht, Reluctant Guardian. */
public class BraskasFinalAeon extends Card {

    public BraskasFinalAeon() {
        addTrigger(Triggers.chapter(1),
                new DiscardEffect(1, DiscardRecipient.EACH_OPPONENT));
        addTrigger(Triggers.chapter(1), new DrawCardEffect(1));
        addTrigger(Triggers.chapter(2),
                new DiscardEffect(1, DiscardRecipient.EACH_OPPONENT));
        addTrigger(Triggers.chapter(2), new DrawCardEffect(1));
        addTrigger(Triggers.chapter(3), new SacrificePermanentsEffect(
                2, new PermanentAllOfPredicate(List.of(new PermanentIsCreaturePredicate())),
                SacrificeRecipient.EACH_OPPONENT));
    }
}
