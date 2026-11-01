package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentBlockingSourcePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

public class BaneclawMarauder extends Card {

    public BaneclawMarauder() {
        addEffect(EffectSlot.ON_BECOMES_BLOCKED,
                new BoostAllCreaturesEffect(-1, -1, new PermanentBlockingSourcePredicate()));
        addTrigger(Triggers.creatureDies(ObjectRelation.ANY),
                new TriggeringPermanentConditionalEffect(
                        new PermanentBlockingSourcePredicate(),
                        new LoseLifeEffect(1, LoseLifeRecipient.DYING_CREATURE_CONTROLLER)));
    }
}
