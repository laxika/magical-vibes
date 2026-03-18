package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RemoveKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.SelfHasKeywordConditionalEffect;

import java.util.List;

@CardRegistration(set = "ISD", collectorNumber = "228")
public class ManorGargoyle extends Card {

    public ManorGargoyle() {
        addEffect(EffectSlot.STATIC, new SelfHasKeywordConditionalEffect(
                Keyword.DEFENDER,
                new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.SELF)
        ));

        addActivatedAbility(new ActivatedAbility(false, "{1}",
                List.of(new RemoveKeywordEffect(Keyword.DEFENDER, GrantScope.SELF),
                        new GrantKeywordEffect(Keyword.FLYING, GrantScope.SELF)),
                "{1}: Until end of turn, Manor Gargoyle loses defender and gains flying."));
    }
}
