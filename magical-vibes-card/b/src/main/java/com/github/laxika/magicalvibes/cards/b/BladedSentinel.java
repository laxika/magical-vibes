package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "MBS", collectorNumber = "98")
public class BladedSentinel extends Card {

    public BladedSentinel() {
        addActivatedAbility(new ActivatedAbility(false, "{W}", List.of(new GrantKeywordEffect(Keyword.VIGILANCE, GrantScope.SELF)), "{W}: Bladed Sentinel gains vigilance until end of turn."));
    }
}
