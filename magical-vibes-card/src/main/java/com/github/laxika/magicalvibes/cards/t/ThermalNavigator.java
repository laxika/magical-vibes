package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

import java.util.List;

@CardRegistration(set = "5DN", collectorNumber = "162")
public class ThermalNavigator extends Card {

    public ThermalNavigator() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new SacrificePermanentCost(new PermanentIsArtifactPredicate(), "an artifact", false),
                        new GrantKeywordEffect(Keyword.FLYING, GrantScope.SELF)),
                "Sacrifice an artifact: This creature gains flying until end of turn."
        ));
    }
}
