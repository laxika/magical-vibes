package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

/**
 * Helm of Chatzuk — {1} Artifact.
 * "{1}, {T}: Target creature gains banding until end of turn."
 */
@CardRegistration(set = "5ED", collectorNumber = "376")
@CardRegistration(set = "4ED", collectorNumber = "324")
public class HelmOfChatzuk extends Card {

    public HelmOfChatzuk() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new GrantKeywordEffect(Keyword.BANDING, GrantScope.TARGET)),
                "{1}, {T}: Target creature gains banding until end of turn.",
                new PermanentPredicateTargetFilter(new PermanentIsCreaturePredicate(), "Target must be a creature")
        ));
    }
}
