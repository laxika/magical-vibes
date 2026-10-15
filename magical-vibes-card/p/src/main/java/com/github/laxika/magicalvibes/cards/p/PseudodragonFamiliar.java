package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "HBG", collectorNumber = "128")
public class PseudodragonFamiliar extends Card {

    public PseudodragonFamiliar() {
        // {2}{U}: Target creature gains flying until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false, "{2}{U}",
                List.of(new GrantKeywordEffect(Keyword.FLYING, GrantScope.TARGET)),
                "{2}{U}: Target creature gains flying until end of turn.",
                TargetFilters.creature()));
    }
}
