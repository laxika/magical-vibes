package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ROE", collectorNumber = "17")
public class DeathlessAngel extends Card {

    public DeathlessAngel() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{W}{W}",
                List.of(new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.TARGET)),
                "{W}{W}: Target creature gains indestructible until end of turn.",
                TargetFilters.creature()
        ));
    }
}
