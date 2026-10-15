package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "WTH", collectorNumber = "150")
public class JabarisBanner extends Card {

    public JabarisBanner() {
        // {1}, {T}: Target creature gains flanking until end of turn.
        addActivatedAbility(new ActivatedAbility(
                true, "{1}",
                List.of(new GrantKeywordEffect(Keyword.FLANKING, GrantScope.TARGET)),
                "{1}, {T}: Target creature gains flanking until end of turn.",
                TargetFilters.creature()));
    }
}
