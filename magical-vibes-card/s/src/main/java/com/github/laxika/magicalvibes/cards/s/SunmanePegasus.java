package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

@CardRegistration(set = "THB", collectorNumber = "38")
public class SunmanePegasus extends Card {

    public SunmanePegasus() {
        addActivatedAbility(new ActivatedAbility(false, "{1}{W}",
                List.of(
                        new GrantKeywordEffect(Keyword.VIGILANCE, GrantScope.SELF),
                        new GrantKeywordEffect(Keyword.LIFELINK, GrantScope.SELF)
                ),
                "{1}{W}: This creature gains vigilance and lifelink until end of turn."));
    }
}
