package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordToSelfEffect;

import java.util.List;

public class MantisEngine extends Card {

    public MantisEngine() {
        addActivatedAbility(new ActivatedAbility(false, "{2}", List.of(new GrantKeywordToSelfEffect(Keyword.FLYING)), false, "{2}: Mantis Engine gains flying until end of turn."));
        addActivatedAbility(new ActivatedAbility(false, "{2}", List.of(new GrantKeywordToSelfEffect(Keyword.FIRST_STRIKE)), false, "{2}: Mantis Engine gains first strike until end of turn."));
    }
}
