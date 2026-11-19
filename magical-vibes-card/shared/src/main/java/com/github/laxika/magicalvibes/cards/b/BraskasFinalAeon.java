package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import com.github.laxika.magicalvibes.model.effect.Subjects;

/** Back face of Jecht, Reluctant Guardian. */
public class BraskasFinalAeon extends Card {

    public BraskasFinalAeon() {
        addTrigger(Triggers.chapter(1), new DiscardEffect(1, Subjects.eachOpponent()), new DrawCardEffect(1));
        addTrigger(Triggers.chapter(2), new DiscardEffect(1, Subjects.eachOpponent()), new DrawCardEffect(1));
        addTrigger(Triggers.chapter(3), new SacrificePermanentsEffect(
                2, new PermanentAllOfPredicate(List.of(new PermanentIsCreaturePredicate())),
                Subjects.eachOpponent()));
    }
}
