package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.FlickerEffect;
import com.github.laxika.magicalvibes.model.effect.FlickerScope;
import com.github.laxika.magicalvibes.model.effect.ReturnTiming;

import java.util.List;

@CardRegistration(set = "TSP", collectorNumber = "17")
public class FlickeringSpirit extends Card {

    public FlickeringSpirit() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{W}",
                List.of(new FlickerEffect(
                        FlickerScope.SELF,
                        null,
                        ReturnTiming.IMMEDIATE,
                        TurnStep.END_STEP,
                        false,
                        null,
                        null,
                        0,
                        false,
                        false)),
                "{3}{W}: Exile this creature, then return it to the battlefield under its owner's control."
        ));
    }
}
