package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExploreEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

public class GuidestoneCompass extends Card {

    public GuidestoneCompass() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new ExploreEffect(true)),
                "{1}, {T}: Target creature you control explores. Activate only as a sorcery.",
                TargetFilters.creatureYouControl(),
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED));
    }
}
