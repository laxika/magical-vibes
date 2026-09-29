package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTargetSagaCardFromGraveyardAndCopyChapterEffect;

@CardRegistration(set = "MB2", collectorNumber = "314")
@CardRegistration(set = "MB2", collectorNumber = "550")
public class TheManyDeedsOfBelzenlok extends Card {

    public TheManyDeedsOfBelzenlok() {
        addEffect(EffectSlot.SAGA_CHAPTER_I,
                new ExileTargetSagaCardFromGraveyardAndCopyChapterEffect(1));
        addEffect(EffectSlot.SAGA_CHAPTER_II,
                new ExileTargetSagaCardFromGraveyardAndCopyChapterEffect(2));
        addEffect(EffectSlot.SAGA_CHAPTER_III,
                new ExileTargetSagaCardFromGraveyardAndCopyChapterEffect(3));
    }
}
