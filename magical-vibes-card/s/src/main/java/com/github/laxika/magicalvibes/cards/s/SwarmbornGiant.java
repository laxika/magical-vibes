package com.github.laxika.magicalvibes.cards.s;

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
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;

import java.util.List;

@CardRegistration(set = "JOU", collectorNumber = "144")
public class SwarmbornGiant extends Card {

    public SwarmbornGiant() {
        SourceIsMonstrous monstrous = new SourceIsMonstrous();

        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_SELF, new SacrificeSelfEffect());
        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}{G}{G}",
                List.of(new MonstrosityEffect(2)),
                "{4}{G}{G}: Monstrosity 2."
        ).withActivationCondition(new NotCondition(monstrous), "This creature is already monstrous"));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(monstrous,
                new GrantKeywordEffect(Keyword.REACH, GrantScope.SELF)));
    }
}
