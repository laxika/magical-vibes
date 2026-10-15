package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTopUntilNonlandWithSuspendEffect;
import com.github.laxika.magicalvibes.model.effect.TimeTravelEffect;

import java.util.List;

@CardRegistration(set = "WHO", collectorNumber = "3")
@CardRegistration(set = "WHO", collectorNumber = "194")
@CardRegistration(set = "WHO", collectorNumber = "446")
@CardRegistration(set = "WHO", collectorNumber = "561")
@CardRegistration(set = "WHO", collectorNumber = "608")
@CardRegistration(set = "WHO", collectorNumber = "1037")
@CardRegistration(set = "WHO", collectorNumber = "1152")
public class TheTenthDoctor extends Card {

    public TheTenthDoctor() {
        addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK, new ExileTopUntilNonlandWithSuspendEffect(3));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{7}",
                List.of(new TimeTravelEffect(3)),
                "{7}: Time travel three times. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED));
    }
}
