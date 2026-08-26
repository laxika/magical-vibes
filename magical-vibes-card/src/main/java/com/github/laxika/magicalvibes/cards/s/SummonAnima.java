package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "FIN", collectorNumber = "120")
public class SummonAnima extends Card {

    public SummonAnima() {
        addEffect(EffectSlot.SAGA_CHAPTER_I,
                SequenceEffect.of(new DrawCardEffect(1), new LoseLifeEffect(1)));
        addEffect(EffectSlot.SAGA_CHAPTER_II,
                SequenceEffect.of(new DrawCardEffect(1), new LoseLifeEffect(1)));
        addEffect(EffectSlot.SAGA_CHAPTER_III,
                SequenceEffect.of(new DrawCardEffect(1), new LoseLifeEffect(1)));
        addEffect(EffectSlot.SAGA_CHAPTER_IV, SequenceEffect.of(
                new SacrificePermanentsEffect(1, new PermanentIsCreaturePredicate(),
                        SacrificeRecipient.EACH_OPPONENT),
                new LoseLifeEffect(3, LoseLifeRecipient.EACH_OPPONENT)));
    }
}
