package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentBlockingSourcePredicate;

public class BaneclawMarauder extends Card {

    public BaneclawMarauder() {
        addEffect(EffectSlot.ON_BECOMES_BLOCKED,
                new BoostAllCreaturesEffect(-1, -1, new PermanentBlockingSourcePredicate()));
        addEffect(EffectSlot.ON_ANY_CREATURE_DIES,
                new TriggeringPermanentConditionalEffect(
                        new PermanentBlockingSourcePredicate(),
                        new LoseLifeEffect(1, LoseLifeRecipient.DYING_CREATURE_CONTROLLER)));
    }
}
