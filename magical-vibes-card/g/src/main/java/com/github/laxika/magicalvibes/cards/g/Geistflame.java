package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;

@CardRegistration(set = "ISD", collectorNumber = "144")
@CardRegistration(set = "DDK", collectorNumber = "61")
public class Geistflame extends Card {

    public Geistflame() {
        addSpellEffect(DealDamageEffect.toAnyTarget(1));
        addCastingOption(new FlashbackCast("{3}{R}"));
    }
}
