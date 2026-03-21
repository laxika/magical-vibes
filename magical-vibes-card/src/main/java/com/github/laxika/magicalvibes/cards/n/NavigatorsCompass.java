package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantBasicLandTypeToTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;

@CardRegistration(set = "DOM", collectorNumber = "225")
public class NavigatorsCompass extends Card {

    public NavigatorsCompass() {
        // When Navigator's Compass enters the battlefield, you gain 3 life.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new GainLifeEffect(3));
        // {T}: Until end of turn, target land you control becomes the basic land type of your
        // choice in addition to its other types.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new GrantBasicLandTypeToTargetEffect(EffectDuration.UNTIL_END_OF_TURN)),
                "{T}: Until end of turn, target land you control becomes the basic land type of your choice in addition to its other types.",
                new ControlledPermanentPredicateTargetFilter(
                        new PermanentIsLandPredicate(),
                        "Target must be a land you control"
                )
        ));
    }
}
