package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RemoveKeywordEffect;

import java.util.List;

@CardRegistration(set = "M11", collectorNumber = "207")
public class GargoyleSentinel extends Card {

    public GargoyleSentinel() {
        addActivatedAbility(new ActivatedAbility(false, "{3}",
                List.of(new RemoveKeywordEffect(Keyword.DEFENDER, GrantScope.SELF),
                        new GrantKeywordEffect(Keyword.FLYING, GrantScope.SELF)),
                "{3}: Until end of turn, Gargoyle Sentinel loses defender and gains flying."));
    }
}
