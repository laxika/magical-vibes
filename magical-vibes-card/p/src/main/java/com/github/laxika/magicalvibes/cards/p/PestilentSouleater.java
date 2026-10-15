package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

@CardRegistration(set = "NPH", collectorNumber = "149")
public class PestilentSouleater extends Card {

    public PestilentSouleater() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{B/P}",
                List.of(new GrantKeywordEffect(Keyword.INFECT, GrantScope.SELF)),
                "{B/P}: Pestilent Souleater gains infect until end of turn."
        ));
    }
}
