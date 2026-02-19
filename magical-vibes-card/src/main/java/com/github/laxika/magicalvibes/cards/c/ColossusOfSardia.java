package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapDuringUntapStepEffect;
import com.github.laxika.magicalvibes.model.effect.UntapSelfEffect;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "317")
public class ColossusOfSardia extends Card {

    public ColossusOfSardia() {
        addEffect(EffectSlot.STATIC, new DoesntUntapDuringUntapStepEffect());

        addActivatedAbility(new ActivatedAbility(
                false,
                "{9}",
                List.of(new UntapSelfEffect()),
                false,
                "{9}: Untap Colossus of Sardia. Activate only during your upkeep.",
                ActivationTimingRestriction.ONLY_DURING_YOUR_UPKEEP
        ));
    }
}
