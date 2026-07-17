package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "5ED", collectorNumber = "49")
public class OrderOfTheWhiteShield extends Card {

    public OrderOfTheWhiteShield() {
        addActivatedAbility(new ActivatedAbility(false, "{W}", List.of(new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.SELF)), "{W}: This creature gains first strike until end of turn."));
        addActivatedAbility(new ActivatedAbility(false, "{W}{W}", List.of(new BoostSelfEffect(1, 0)), "{W}{W}: This creature gets +1/+0 until end of turn."));
    }
}
