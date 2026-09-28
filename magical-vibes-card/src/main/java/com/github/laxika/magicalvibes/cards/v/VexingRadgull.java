package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.TargetPlayerHasRadCounters;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GiveTargetPlayerRadCountersEffect;
import com.github.laxika.magicalvibes.model.effect.ProliferateEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "PIP", collectorNumber = "41")
@CardRegistration(set = "PIP", collectorNumber = "569")
public class VexingRadgull extends Card {

    public VexingRadgull() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                SequenceEffect.of(
                        new ConditionalEffect(new TargetPlayerHasRadCounters(), new ProliferateEffect()),
                        new ConditionalEffect(new NotCondition(new TargetPlayerHasRadCounters()),
                                new GiveTargetPlayerRadCountersEffect(2))
                ));
    }
}
