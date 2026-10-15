package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

@CardRegistration(set = "GTC", collectorNumber = "109")
public class ToweringThunderfist extends Card {

    public ToweringThunderfist() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{W}",
                List.of(new GrantKeywordEffect(Keyword.VIGILANCE, GrantScope.SELF)),
                "{W}: Towering Thunderfist gains vigilance until end of turn."
        ));
    }
}
