package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PossibilityStormCastTriggerEffect;

@CardRegistration(set = "DGM", collectorNumber = "34")
public class PossibilityStorm extends Card {

    public PossibilityStorm() {
        addEffect(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL, new PossibilityStormCastTriggerEffect());
    }
}
