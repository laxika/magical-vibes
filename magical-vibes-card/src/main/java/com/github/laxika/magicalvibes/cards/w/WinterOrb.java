package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.StaticOrbEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "5ED", collectorNumber = "408")
public class WinterOrb extends Card {

    public WinterOrb() {
        // Static: while this artifact is untapped, players can't untap more than one land
        // during their untap steps. Non-land permanents are unaffected and untap normally.
        addEffect(EffectSlot.STATIC, new StaticOrbEffect(1, new PermanentIsLandPredicate(), true));
    }
}
