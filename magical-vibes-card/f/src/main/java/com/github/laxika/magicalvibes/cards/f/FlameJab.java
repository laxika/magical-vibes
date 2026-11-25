package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Retrace;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;

@CardRegistration(set = "EVE", collectorNumber = "53")
@CardRegistration(set = "EMA", collectorNumber = "131")
public class FlameJab extends Card {

    public FlameJab() {
        addSpellEffect(DealDamageEffect.toAnyTarget(1));
        addCastingOption(new Retrace());
    }
}
