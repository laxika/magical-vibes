package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

@CardRegistration(set = "INV", collectorNumber = "195")
public class LlanowarCavalry extends Card {

    public LlanowarCavalry() {
        addActivatedAbility(new ActivatedAbility(false, "{W}", List.of(new GrantKeywordEffect(Keyword.VIGILANCE, GrantScope.SELF)), "{W}: Llanowar Cavalry gains vigilance until end of turn."));
    }
}
