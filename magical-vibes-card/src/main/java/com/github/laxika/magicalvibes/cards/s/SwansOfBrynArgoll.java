package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventDamageToSelfAndSourceControllerDrawsEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "SHM", collectorNumber = "151")
public class SwansOfBrynArgoll extends Card {

    public SwansOfBrynArgoll() {
        addEffect(EffectSlot.STATIC, new PreventDamageToSelfAndSourceControllerDrawsEffect());
    }
}
