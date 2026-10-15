package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureWhileSourceTappedEffect;
import com.github.laxika.magicalvibes.model.effect.MayNotUntapDuringUntapStepEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "USG", collectorNumber = "294")
public class Endoskeleton extends Card {

    public Endoskeleton() {
        addEffect(EffectSlot.STATIC, new MayNotUntapDuringUntapStepEffect());
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new BoostTargetCreatureWhileSourceTappedEffect(0, 3)),
                "{2}, {T}: Target creature gets +0/+3 for as long as this artifact remains tapped.",
                TargetFilters.creature()
        ));
    }
}
