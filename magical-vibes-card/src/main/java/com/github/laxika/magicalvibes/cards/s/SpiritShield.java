package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureWhileSourceTappedEffect;
import com.github.laxika.magicalvibes.model.effect.MayNotUntapDuringUntapStepEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "FEM", collectorNumber = "90")
@CardRegistration(set = "FEM", collectorNumber = "149")
public class SpiritShield extends Card {

    public SpiritShield() {
        addEffect(EffectSlot.STATIC, new MayNotUntapDuringUntapStepEffect());
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new BoostTargetCreatureWhileSourceTappedEffect(0, 2)),
                "{2}, {T}: Target creature gets +0/+2 for as long as this artifact remains tapped.",
                TargetFilters.creature()
        ));
    }
}
