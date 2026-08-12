package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MustBlockSourceEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ZEN", collectorNumber = "190")
public class TurntimberBasilisk extends Card {

    public TurntimberBasilisk() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.ON_ALLY_LAND_ENTERS_BATTLEFIELD,
                        new MayEffect(new MustBlockSourceEffect(null),
                                "Have target creature block Turntimber Basilisk this turn if able?"));
    }
}
