package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureWhileSourceTappedEffect;
import com.github.laxika.magicalvibes.model.effect.MayNotUntapDuringUntapStepEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "NEM", collectorNumber = "131")
public class FlowstoneArmor extends Card {

    public FlowstoneArmor() {
        // You may choose not to untap this artifact during your untap step.
        addEffect(EffectSlot.STATIC, new MayNotUntapDuringUntapStepEffect());

        // {3}, {T}: Target creature gets +1/-1 for as long as this artifact remains tapped.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}",
                List.of(new BoostTargetCreatureWhileSourceTappedEffect(1, -1)),
                "{3}, {T}: Target creature gets +1/-1 for as long as this artifact remains tapped.",
                TargetFilters.creature()
        ));
    }
}
