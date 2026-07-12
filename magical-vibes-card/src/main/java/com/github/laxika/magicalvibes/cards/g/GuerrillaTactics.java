package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "211")
@CardRegistration(set = "9ED", collectorNumber = "196")
@CardRegistration(set = "8ED", collectorNumber = "192")
public class GuerrillaTactics extends Card {

    public GuerrillaTactics() {
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(2));
        addEffect(EffectSlot.ON_SELF_DISCARDED_BY_OPPONENT, new DealDamageToAnyTargetEffect(4));
    }
}
