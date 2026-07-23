package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "5ED", collectorNumber = "171")
@CardRegistration(set = "ICE", collectorNumber = "138")
public class KnightOfStromgald extends Card {

    public KnightOfStromgald() {
        addActivatedAbility(new ActivatedAbility(false, "{B}", List.of(new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.SELF)), "{B}: This creature gains first strike until end of turn."));
        addActivatedAbility(new ActivatedAbility(false, "{B}{B}", List.of(new BoostSelfEffect(1, 0)), "{B}{B}: This creature gets +1/+0 until end of turn."));
    }
}
