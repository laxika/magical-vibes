package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

@CardRegistration(set = "GTC", collectorNumber = "202")
public class TruefirePaladin extends Card {

    public TruefirePaladin() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{R}{W}",
                List.of(new BoostSelfEffect(2, 0)),
                "{R}{W}: This creature gets +2/+0 until end of turn."
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{R}{W}",
                List.of(new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.SELF)),
                "{R}{W}: This creature gains first strike until end of turn."
        ));
    }
}
