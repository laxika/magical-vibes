package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

/** Back face of Jecht, Reluctant Guardian. */
public class BraskasFinalAeon extends Card {

    public BraskasFinalAeon() {
        addEffect(EffectSlot.SAGA_CHAPTER_I,
                new DiscardEffect(1, DiscardRecipient.EACH_OPPONENT));
        addEffect(EffectSlot.SAGA_CHAPTER_I, new DrawCardEffect(1));
        addEffect(EffectSlot.SAGA_CHAPTER_II,
                new DiscardEffect(1, DiscardRecipient.EACH_OPPONENT));
        addEffect(EffectSlot.SAGA_CHAPTER_II, new DrawCardEffect(1));
        addEffect(EffectSlot.SAGA_CHAPTER_III, new SacrificePermanentsEffect(
                2, new PermanentAllOfPredicate(List.of(new PermanentIsCreaturePredicate())),
                SacrificeRecipient.EACH_OPPONENT));
    }
}
