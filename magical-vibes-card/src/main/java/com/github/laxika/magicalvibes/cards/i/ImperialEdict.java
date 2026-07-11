package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerChoosesCreatureDestroyEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "PTK", collectorNumber = "77")
public class ImperialEdict extends Card {

    public ImperialEdict() {
        addEffect(EffectSlot.SPELL, new TargetPlayerChoosesCreatureDestroyEffect());
    }
}
