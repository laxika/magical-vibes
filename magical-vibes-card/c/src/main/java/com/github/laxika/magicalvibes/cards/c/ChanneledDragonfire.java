package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.HarmonizeCast;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

@CardRegistration(set = "TDM", collectorNumber = "102")
public class ChanneledDragonfire extends Card {

    public ChanneledDragonfire() {
        addSpellEffect(new DealDamageToAnyTargetEffect(2));
        addCastingOption(new HarmonizeCast("{5}{R}{R}"));
    }
}
