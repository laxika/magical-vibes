package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

@CardRegistration(set = "MH2", collectorNumber = "133")
public class Kaleidoscorch extends Card {

    public Kaleidoscorch() {
        addSpellEffect(new DealDamageToAnyTargetEffect(new XValue()));
        addCastingOption(new FlashbackCast("{4}{R}"));
    }
}
