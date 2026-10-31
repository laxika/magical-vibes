package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

@CardRegistration(set = "SOS", collectorNumber = "135")
public class TomeBlast extends Card {

    public TomeBlast() {
        addSpellEffect(new DealDamageToAnyTargetEffect(2));
        addCastingOption(new FlashbackCast("{4}{R}"));
    }
}
