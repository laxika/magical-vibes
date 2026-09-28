package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.FlickerEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "HBG", collectorNumber = "246")
public class OjiTheExquisiteBlade extends Card {

    public OjiTheExquisiteBlade() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new GainLifeEffect(2));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ScryEffect(2));

        var creatureYouControl = TargetFilters.creatureYouControl();
        target(creatureYouControl, 0, 1)
                .addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                        SpellCastTriggerEffect.nth(2, null,
                                List.of(FlickerEffect.flickerTarget()), creatureYouControl));
    }
}
