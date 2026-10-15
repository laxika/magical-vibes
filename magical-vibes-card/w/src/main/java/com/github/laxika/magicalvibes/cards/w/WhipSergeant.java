package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "9ED", collectorNumber = "227")
@CardRegistration(set = "PCY", collectorNumber = "107")
public class WhipSergeant extends Card {

    public WhipSergeant() {
        // {R}: Target creature gains haste until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false, "{R}",
                List.of(new GrantKeywordEffect(Keyword.HASTE, GrantScope.TARGET)),
                "{R}: Target creature gains haste until end of turn.",
                TargetFilters.creature()));
    }
}
