package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.condition.TwoOrMoreSpellsCastLastTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;

public class FlameheartWerewolf extends Card {

    public FlameheartWerewolf() {
        DealDamageToTargetCreatureEffect damage = new DealDamageToTargetCreatureEffect(2);
        addEffect(EffectSlot.ON_BLOCK, damage);
        addEffect(EffectSlot.ON_BECOMES_BLOCKED, damage, TriggerMode.PER_BLOCKER);

        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED,
                new ConditionalEffect(new TwoOrMoreSpellsCastLastTurn(), new TransformSelfEffect()));
    }
}
