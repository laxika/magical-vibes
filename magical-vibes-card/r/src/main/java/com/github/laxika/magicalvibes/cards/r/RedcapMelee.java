package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureOrPlaneswalkerThenSacrificeLandIfNonredEffect;

@CardRegistration(set = "ELD", collectorNumber = "135")
public class RedcapMelee extends Card {

    public RedcapMelee() {
        addEffect(EffectSlot.SPELL,
                new DealDamageToTargetCreatureOrPlaneswalkerThenSacrificeLandIfNonredEffect(4));
    }
}
