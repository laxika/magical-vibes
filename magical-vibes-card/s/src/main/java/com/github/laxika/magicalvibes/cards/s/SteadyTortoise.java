package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.h.HarriedDash;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostCardEffect;

@CardRegistration(set = "YWOE", collectorNumber = "25")
public class SteadyTortoise extends Card {

    public SteadyTortoise() {
        setBackFaceCard(new HarriedDash());
        addCastingOption(new AdventureCast("{R}"));

        PerpetuallyBoostCardEffect attackBoost = new PerpetuallyBoostCardEffect(this, 1, 1);
        addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK, attackBoost);
        addEffect(EffectSlot.EXILE_ON_ALLY_CREATURES_ATTACK, attackBoost);
    }

    @Override
    public String getBackFaceClassName() {
        return "HarriedDash";
    }
}
