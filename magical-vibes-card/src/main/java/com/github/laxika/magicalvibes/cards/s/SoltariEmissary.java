package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

@CardRegistration(set = "TMP", collectorNumber = "42")
public class SoltariEmissary extends Card {

    public SoltariEmissary() {
        // {W}: This creature gains shadow until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{W}",
                List.of(new GrantKeywordEffect(Keyword.SHADOW, GrantScope.SELF)),
                "{W}: This creature gains shadow until end of turn."
        ));
    }
}
