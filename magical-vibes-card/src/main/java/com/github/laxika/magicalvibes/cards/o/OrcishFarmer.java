package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantBasicLandTypeToTargetEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "5ED", collectorNumber = "256")
public class OrcishFarmer extends Card {

    public OrcishFarmer() {
        // {T}: Target land becomes a Swamp until its controller's next untap step.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new GrantBasicLandTypeToTargetEffect(
                        EffectDuration.UNTIL_CONTROLLERS_NEXT_UNTAP_STEP, CardSubtype.SWAMP, true)),
                "{T}: Target land becomes a Swamp until its controller's next untap step.",
                new PermanentPredicateTargetFilter(
                        new PermanentIsLandPredicate(),
                        "Target must be a land"
                )
        ));
    }
}
