package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Retrace;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesLifeAndControllerGainsLifeEffect;

@CardRegistration(set = "EVE", collectorNumber = "46")
public class SyphonLife extends Card {

    public SyphonLife() {
        addEffect(EffectSlot.SPELL, new TargetPlayerLosesLifeAndControllerGainsLifeEffect(2, 2));
        addCastingOption(new Retrace());
    }
}
