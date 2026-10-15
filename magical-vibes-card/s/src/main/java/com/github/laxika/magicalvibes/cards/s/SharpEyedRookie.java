package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EvolveTriggerEffect;

@CardRegistration(set = "MKM", collectorNumber = "176")
@CardRegistration(set = "MKM", collectorNumber = "353")
public class SharpEyedRookie extends Card {

    public SharpEyedRookie() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD, new EvolveTriggerEffect());
        addEffect(EffectSlot.ON_SELF_EVOLVES, CreateTokenEffect.ofClueToken(1));
    }
}
