package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

public class StranglingGrasp extends Card {

    public StranglingGrasp() {
        addEffect(EffectSlot.ENCHANTED_PERMANENT_CONTROLLER_UPKEEP_TRIGGERED,
                SequenceEffect.of(
                        new SacrificePermanentsEffect(1,
                                new PermanentNotPredicate(new PermanentIsLandPredicate()),
                                SacrificeRecipient.TARGET_PLAYER),
                        new LoseLifeEffect(1, LoseLifeRecipient.TARGET_PLAYER)));
    }
}
