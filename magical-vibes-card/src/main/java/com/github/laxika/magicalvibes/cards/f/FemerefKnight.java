package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

@CardRegistration(set = "MIR", collectorNumber = "18")
public class FemerefKnight extends Card {

    public FemerefKnight() {
        addActivatedAbility(new ActivatedAbility(false, "{W}", List.of(new GrantKeywordEffect(Keyword.VIGILANCE, GrantScope.SELF)), "{W}: This creature gains vigilance until end of turn."));
    }
}
