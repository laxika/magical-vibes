package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveKeywordEffect;
import com.github.laxika.magicalvibes.model.condition.SelfHasKeyword;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;

import com.github.laxika.magicalvibes.model.effect.Subjects;
import java.util.List;

@CardRegistration(set = "ISD", collectorNumber = "228")
public class ManorGargoyle extends Card {

    public ManorGargoyle() {
        addStaticEffect(new ConditionalEffect(new SelfHasKeyword(Keyword.DEFENDER), new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, Subjects.source())
        ));

        addActivatedAbility(new ActivatedAbility(false, "{1}",
                List.of(new RemoveKeywordEffect(Keyword.DEFENDER, Subjects.source()),
                        new GrantKeywordEffect(Keyword.FLYING, Subjects.source())),
                "{1}: Until end of turn, Manor Gargoyle loses defender and gains flying."));
    }
}
