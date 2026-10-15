package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

@CardRegistration(set = "ISD", collectorNumber = "151")
public class KessigWolf extends Card {

    public KessigWolf() {
        addActivatedAbility(new ActivatedAbility(false, "{1}{R}", List.of(new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.SELF)), "{1}{R}: Kessig Wolf gains first strike until end of turn."));
    }
}
