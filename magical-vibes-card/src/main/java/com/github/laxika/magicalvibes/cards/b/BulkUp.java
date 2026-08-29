package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.TargetPower;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "FDN", collectorNumber = "80")
public class BulkUp extends Card {

    public BulkUp() {
        target(TargetFilters.creature()).addEffect(EffectSlot.SPELL,
                new BoostTargetCreatureEffect(new TargetPower(), new Fixed(0)));
        addCastingOption(new FlashbackCast("{4}{R}{R}"));
    }
}
