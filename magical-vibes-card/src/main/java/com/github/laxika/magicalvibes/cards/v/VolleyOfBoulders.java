package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.effect.DealDividedDamageEffect;

@CardRegistration(set = "ODY", collectorNumber = "227")
public class VolleyOfBoulders extends Card {

    public VolleyOfBoulders() {
        addEffect(EffectSlot.SPELL, DealDividedDamageEffect.chosenAmongAnyTargets(6));
        addCastingOption(new FlashbackCast("{R}{R}{R}{R}{R}{R}"));
    }
}
