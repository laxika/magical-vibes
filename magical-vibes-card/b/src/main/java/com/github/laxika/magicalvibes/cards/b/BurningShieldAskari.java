package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

@CardRegistration(set = "MIR", collectorNumber = "162")
public class BurningShieldAskari extends Card {

    public BurningShieldAskari() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{R}{R}",
                List.of(new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.SELF)),
                "{R}{R}: Burning Shield Askari gains first strike until end of turn."
        ));
    }
}
