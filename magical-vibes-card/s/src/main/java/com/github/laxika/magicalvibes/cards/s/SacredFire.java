package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "MID", collectorNumber = "239")
@CardRegistration(set = "DBL", collectorNumber = "239")
public class SacredFire extends Card {

    public SacredFire() {
        addSpellEffect(new DealDamageToAnyTargetEffect(2));
        addSpellEffect(new GainLifeEffect(2));
        addCastingOption(new FlashbackCast("{4}{R}{W}"));
    }
}
