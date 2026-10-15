package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantDuration;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "M20", collectorNumber = "286")
public class MuYanlingCelestialWind extends Card {

    public MuYanlingCelestialWind() {
        // +1: Until your next turn, up to one target creature gets -5/-0.
        addActivatedAbility(new ActivatedAbility(
                false, null,
                List.of(new BoostTargetCreatureEffect(-5, 0, GrantDuration.UNTIL_YOUR_NEXT_TURN)),
                "+1: Until your next turn, up to one target creature gets -5/-0.",
                null, +1, null, null,
                List.<TargetFilter>of(TargetFilters.creature()), 0, 1
        ));

        // −3: Return up to two target creatures to their owners' hands.
        addActivatedAbility(new ActivatedAbility(
                false, null,
                List.of(ReturnToHandEffect.target()),
                "−3: Return up to two target creatures to their owners' hands.",
                null, -3, null, null,
                List.<TargetFilter>of(TargetFilters.creature()), 0, 2
        ));

        // −7: Creatures you control with flying get +5/+5 until end of turn.
        addActivatedAbility(new ActivatedAbility(
                -7,
                List.of(new BoostAllOwnCreaturesEffect(5, 5,
                        new PermanentHasKeywordPredicate(Keyword.FLYING))),
                "−7: Creatures you control with flying get +5/+5 until end of turn."
        ));
    }
}
