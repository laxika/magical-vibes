package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GrantChosenSnowLandwalkToTargetEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetPermanentToHandAtEndStepEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ICE", collectorNumber = "174")
public class BarbarianGuides extends Card {

    public BarbarianGuides() {
        // {2}{R}, {T}: Choose a land type. Target creature you control gains snow landwalk of the
        // chosen type until end of turn. Return that creature to its owner's hand at the beginning
        // of the next end step.
        addActivatedAbility(new ActivatedAbility(
                true, "{2}{R}",
                List.of(
                        new GrantChosenSnowLandwalkToTargetEffect(),
                        new ReturnTargetPermanentToHandAtEndStepEffect()
                ),
                "{2}{R}, {T}: Choose a land type. Target creature you control gains snow landwalk "
                        + "of the chosen type until end of turn. Return that creature to its owner's "
                        + "hand at the beginning of the next end step.",
                TargetFilters.creatureYouControl()
        ));
    }
}
