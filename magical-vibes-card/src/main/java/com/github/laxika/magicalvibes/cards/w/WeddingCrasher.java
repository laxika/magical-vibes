package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

public class WeddingCrasher extends Card {

    private static final DrawCardEffect DRAW_CARD = new DrawCardEffect(1);

    public WeddingCrasher() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES, new TriggeringCardConditionalEffect(
                new CardAnyOfPredicate(List.of(
                        new CardSubtypePredicate(CardSubtype.WOLF),
                        new CardSubtypePredicate(CardSubtype.WEREWOLF))),
                DRAW_CARD));
        addEffect(EffectSlot.ON_DEATH, DRAW_CARD);
    }
}
