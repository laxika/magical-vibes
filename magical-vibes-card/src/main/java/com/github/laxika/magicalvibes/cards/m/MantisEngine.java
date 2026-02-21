package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect.Scope;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "333")
public class MantisEngine extends Card {

    public MantisEngine() {
        addActivatedAbility(new ActivatedAbility(false, "{2}", List.of(new GrantKeywordEffect(Keyword.FLYING, Scope.SELF)), false, "{2}: Mantis Engine gains flying until end of turn."));
        addActivatedAbility(new ActivatedAbility(false, "{2}", List.of(new GrantKeywordEffect(Keyword.FIRST_STRIKE, Scope.SELF)), false, "{2}: Mantis Engine gains first strike until end of turn."));
    }
}
