package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

@CardRegistration(set = "M14", collectorNumber = "39")
public class StonehornChanter extends Card {

    public StonehornChanter() {
        // {5}{W}: This creature gains vigilance and lifelink until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{5}{W}",
                List.of(
                        new GrantKeywordEffect(Keyword.VIGILANCE, GrantScope.SELF),
                        new GrantKeywordEffect(Keyword.LIFELINK, GrantScope.SELF)
                ),
                "{5}{W}: This creature gains vigilance and lifelink until end of turn."
        ));
    }
}
