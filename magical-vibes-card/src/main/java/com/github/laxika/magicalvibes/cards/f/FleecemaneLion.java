package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceIsMonstrous;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MonstrosityEffect;

import java.util.List;

@CardRegistration(set = "THS", collectorNumber = "193")
public class FleecemaneLion extends Card {

    public FleecemaneLion() {
        SourceIsMonstrous monstrous = new SourceIsMonstrous();

        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{G}{W}",
                List.of(new MonstrosityEffect(1)),
                "{3}{G}{W}: Monstrosity 1."
        ).withActivationCondition(new NotCondition(monstrous), "This creature is already monstrous"));

        addEffect(EffectSlot.STATIC, new ConditionalEffect(monstrous,
                new GrantKeywordEffect(Keyword.HEXPROOF, GrantScope.SELF)));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(monstrous,
                new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.SELF)));
    }
}
