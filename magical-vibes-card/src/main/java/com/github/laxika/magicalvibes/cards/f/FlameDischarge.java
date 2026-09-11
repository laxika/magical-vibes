package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.condition.ControlledModifiedCreatureAsCast;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureOrPlaneswalkerEffect;

@CardRegistration(set = "NEO", collectorNumber = "142")
public class FlameDischarge extends Card {

    public FlameDischarge() {
        addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(
                new ControlledModifiedCreatureAsCast(),
                new DealDamageToTargetCreatureOrPlaneswalkerEffect(new XValue()),
                new DealDamageToTargetCreatureOrPlaneswalkerEffect(new Sum(new XValue(), new Fixed(2)))
        ));
    }
}
