package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.ExileSelfCost;
import com.github.laxika.magicalvibes.model.effect.FlickerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "LGN", collectorNumber = "18")
public class PlanarGuide extends Card {

    public PlanarGuide() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{W}",
                List.of(new ExileSelfCost(),
                        FlickerEffect.exileAllPlayersPermanentsReturnAtStep(
                                new PermanentIsCreaturePredicate(), TurnStep.END_STEP)),
                "{3}{W}, Exile this creature: Exile all creatures. At the beginning of the next end step, return those cards to the battlefield under their owners' control."
        ));
    }
}
