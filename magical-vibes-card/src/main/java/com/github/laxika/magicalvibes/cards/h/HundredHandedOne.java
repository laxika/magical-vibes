package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceIsMonstrous;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantAdditionalBlockEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MonstrosityEffect;

import java.util.List;

@CardRegistration(set = "THS", collectorNumber = "20")
public class HundredHandedOne extends Card {

    public HundredHandedOne() {
        SourceIsMonstrous monstrous = new SourceIsMonstrous();

        addActivatedAbility(new ActivatedAbility(false, "{3}{W}{W}{W}", List.of(new MonstrosityEffect(3)),
                "{3}{W}{W}{W}: Monstrosity 3.")
                .withActivationCondition(new NotCondition(monstrous), "This creature is already monstrous"));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(monstrous,
                new GrantKeywordEffect(Keyword.REACH, GrantScope.SELF)));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(monstrous,
                new GrantAdditionalBlockEffect(99)));
    }
}
