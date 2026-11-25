package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControllerHandEmpty;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;

@CardRegistration(set = "DIS", collectorNumber = "59")
@CardRegistration(set = "GK2", collectorNumber = "55")
public class CacklingFlames extends Card {

    public CacklingFlames() {
        addSpellEffect(new ConditionalReplacementEffect(
                new ControllerHandEmpty(),
                DealDamageEffect.toAnyTarget(3),
                DealDamageEffect.toAnyTarget(5)
        ));
    }
}
