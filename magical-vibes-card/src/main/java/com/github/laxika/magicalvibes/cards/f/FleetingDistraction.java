package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "AVR", collectorNumber = "53")
@CardRegistration(set = "FDN", collectorNumber = "155")
@CardRegistration(set = "ROE", collectorNumber = "67")
public class FleetingDistraction extends Card {

    public FleetingDistraction() {
        addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(-1, 0));
        addEffect(EffectSlot.SPELL, new DrawCardEffect());
    }
}
