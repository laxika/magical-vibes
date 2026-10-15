package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "MIR", collectorNumber = "91")
public class ShaperGuildmage extends Card {

    public ShaperGuildmage() {
        addActivatedAbility(new ActivatedAbility(true, "{W}",
                List.of(new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.TARGET)),
                "{W}, {T}: Target creature gains first strike until end of turn.",
                TargetFilters.creature()));

        addActivatedAbility(new ActivatedAbility(true, "{B}",
                List.of(new BoostTargetCreatureEffect(1, 0)),
                "{B}, {T}: Target creature gets +1/+0 until end of turn.",
                TargetFilters.creature()));
    }
}
