package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.condition.TwoOrMoreSpellsCastLastTurn;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentBlockingSourcePredicate;

public class BaneclawMarauder extends Card {

    public BaneclawMarauder() {
        addEffect(EffectSlot.ON_BECOMES_BLOCKED,
                new BoostTargetCreatureEffect(-1, -1), TriggerMode.PER_BLOCKER);
        addEffect(EffectSlot.ON_ANY_CREATURE_DIES,
                new TriggeringPermanentConditionalEffect(
                        new PermanentBlockingSourcePredicate(),
                        new LoseLifeEffect(1, LoseLifeRecipient.DYING_CREATURE_CONTROLLER)));
        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED,
                new ConditionalEffect(new TwoOrMoreSpellsCastLastTurn(), new TransformSelfEffect()));
    }
}
