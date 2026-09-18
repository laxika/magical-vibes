package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.CrewCost;
import com.github.laxika.magicalvibes.model.effect.FlickerEffect;
import com.github.laxika.magicalvibes.model.effect.FlickerScope;
import com.github.laxika.magicalvibes.model.effect.ReturnTiming;
import com.github.laxika.magicalvibes.model.filter.PermanentCrewedBySourceThisTurnPredicate;

import java.util.List;

@CardRegistration(set = "DMU", collectorNumber = "230")
public class GoldenArgosy extends Card {

    public GoldenArgosy() {
        addEffect(EffectSlot.ON_ATTACK, new FlickerEffect(
                FlickerScope.CONTROLLERS_PERMANENTS,
                new PermanentCrewedBySourceThisTurnPredicate(),
                ReturnTiming.AT_STEP,
                TurnStep.END_STEP,
                true,
                null,
                null,
                0,
                false,
                false
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new CrewCost(1), AnimatePermanentsEffect.crew()),
                "Crew 1"
        ));
    }
}
