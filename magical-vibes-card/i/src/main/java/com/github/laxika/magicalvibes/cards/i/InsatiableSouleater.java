package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

@CardRegistration(set = "NPH", collectorNumber = "140")
public class InsatiableSouleater extends Card {

    public InsatiableSouleater() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{G/P}",
                List.of(new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.SELF)),
                "{G/P}: Insatiable Souleater gains trample until end of turn."
        ));
    }
}
