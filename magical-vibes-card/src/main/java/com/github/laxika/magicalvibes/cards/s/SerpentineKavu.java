package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

@CardRegistration(set = "INV", collectorNumber = "211")
public class SerpentineKavu extends Card {

    public SerpentineKavu() {
        // {R}: This creature gains haste until end of turn.
        addActivatedAbility(new ActivatedAbility(false, "{R}",
                List.of(new GrantKeywordEffect(Keyword.HASTE, GrantScope.SELF)),
                "{R}: This creature gains haste until end of turn."));
    }
}
