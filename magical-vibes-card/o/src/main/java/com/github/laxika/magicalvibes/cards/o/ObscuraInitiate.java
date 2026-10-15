package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

@CardRegistration(set = "SNC", collectorNumber = "50")
public class ObscuraInitiate extends Card {

    public ObscuraInitiate() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{W/B}",
                List.of(new GrantKeywordEffect(Keyword.LIFELINK, GrantScope.SELF)),
                "{1}{W/B}: This creature gains lifelink until end of turn."
        ));
    }
}
