package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

@CardRegistration(set = "BRO", collectorNumber = "134")
public class FallajiChaindancer extends Card {

    public FallajiChaindancer() {
        addActivatedAbility(new ActivatedAbility(false, "{2}",
                List.of(new GrantKeywordEffect(Keyword.DOUBLE_STRIKE, GrantScope.SELF)),
                "{2}: This creature gains double strike until end of turn."));
    }
}
