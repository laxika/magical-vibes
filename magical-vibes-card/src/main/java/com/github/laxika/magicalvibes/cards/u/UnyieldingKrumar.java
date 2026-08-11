package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

@CardRegistration(set = "KTK", collectorNumber = "94")
public class UnyieldingKrumar extends Card {

    public UnyieldingKrumar() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{W}",
                List.of(new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.SELF)),
                "{1}{W}: This creature gains first strike until end of turn."
        ));
    }
}
