package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCommanderPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "C21", collectorNumber = "81")
@CardRegistration(set = "DSC", collectorNumber = "325")
public class WitchsClinic extends Card {

    public WitchsClinic() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new GrantKeywordEffect(
                        Keyword.LIFELINK,
                        GrantScope.TARGET,
                        new PermanentIsCommanderPredicate())),
                "{2}, {T}: Target commander gains lifelink until end of turn.",
                new PermanentPredicateTargetFilter(
                        new PermanentIsCommanderPredicate(),
                        "Target must be a commander")));
    }
}
