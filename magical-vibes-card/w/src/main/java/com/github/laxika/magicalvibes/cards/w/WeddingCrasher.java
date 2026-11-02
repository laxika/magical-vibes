package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

public class WeddingCrasher extends Card {

    private static final DrawCardEffect DRAW_CARD = new DrawCardEffect(1);

    public WeddingCrasher() {
        addTrigger(Triggers.creatureDies(
                        new PermanentAnyOfPredicate(List.of(
                                new PermanentHasSubtypePredicate(CardSubtype.WOLF),
                                new PermanentHasSubtypePredicate(CardSubtype.WEREWOLF))),
                        ObjectRelation.THIS_OR_ANOTHER_YOU_CONTROL),
                DRAW_CARD);
    }
}
