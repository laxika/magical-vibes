package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

/**
 * Baton of Morale — {2} Artifact.
 * "{2}: Target creature gains banding until end of turn."
 */
@CardRegistration(set = "ICE", collectorNumber = "313")
public class BatonOfMorale extends Card {

    public BatonOfMorale() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(new GrantKeywordEffect(Keyword.BANDING, GrantScope.TARGET)),
                "{2}: Target creature gains banding until end of turn.",
                TargetFilters.creature()
        ));
    }
}
