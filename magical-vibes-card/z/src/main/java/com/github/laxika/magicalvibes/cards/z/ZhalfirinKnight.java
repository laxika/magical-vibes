package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

@CardRegistration(set = "MIR", collectorNumber = "50")
public class ZhalfirinKnight extends Card {

    public ZhalfirinKnight() {
        addActivatedAbility(new ActivatedAbility(false, "{W}{W}", List.of(new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.SELF)), "{W}{W}: Zhalfirin Knight gains first strike until end of turn."));
    }
}
