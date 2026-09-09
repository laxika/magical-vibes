package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

@CardRegistration(set = "BFZ", collectorNumber = "89")
public class WindriderPatrol extends Card {

    public WindriderPatrol() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new ScryEffect(2));
    }
}
