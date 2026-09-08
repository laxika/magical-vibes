package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

@CardRegistration(set = "KLD", collectorNumber = "238")
public class WeldfastMonitor extends Card {

    public WeldfastMonitor() {
        addActivatedAbility(new ActivatedAbility(
                false, "{R}",
                List.of(new GrantKeywordEffect(Keyword.MENACE, GrantScope.SELF)),
                "{R}: This creature gains menace until end of turn."
        ));
    }
}
