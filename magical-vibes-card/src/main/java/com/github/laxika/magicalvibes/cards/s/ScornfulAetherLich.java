package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "CON", collectorNumber = "34")
public class ScornfulAetherLich extends Card {

    public ScornfulAetherLich() {
        // {W}{B}: This creature gains fear and vigilance until end of turn.
        addActivatedAbility(new ActivatedAbility(false, "{W}{B}",
                List.of(new GrantKeywordEffect(Set.of(Keyword.FEAR, Keyword.VIGILANCE), GrantScope.SELF)),
                "{W}{B}: This creature gains fear and vigilance until end of turn."));
    }
}
