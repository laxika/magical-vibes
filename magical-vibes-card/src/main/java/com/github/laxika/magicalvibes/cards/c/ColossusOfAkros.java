package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceIsMonstrous;
import com.github.laxika.magicalvibes.model.effect.CanAttackAsThoughNoDefenderEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MonstrosityEffect;

import java.util.List;

@CardRegistration(set = "THS", collectorNumber = "214")
public class ColossusOfAkros extends Card {

    public ColossusOfAkros() {
        SourceIsMonstrous monstrous = new SourceIsMonstrous();

        addActivatedAbility(new ActivatedAbility(
                false,
                "{10}",
                List.of(new MonstrosityEffect(10)),
                "{10}: Monstrosity 10."
        ).withActivationCondition(new NotCondition(monstrous), "This creature is already monstrous"));

        addEffect(EffectSlot.STATIC, new ConditionalEffect(monstrous,
                new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.SELF)));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(monstrous,
                new CanAttackAsThoughNoDefenderEffect()));
    }
}
