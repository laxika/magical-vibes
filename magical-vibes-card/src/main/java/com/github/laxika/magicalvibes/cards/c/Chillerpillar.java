package com.github.laxika.magicalvibes.cards.c;

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

@CardRegistration(set = "MH1", collectorNumber = "43")
public class Chillerpillar extends Card {

    public Chillerpillar() {
        SourceIsMonstrous monstrous = new SourceIsMonstrous();

        addActivatedAbility(new ActivatedAbility(false, "{4}{S}{S}", List.of(new MonstrosityEffect(2)),
                "{4}{S}{S}: Monstrosity 2.")
                .withActivationCondition(new NotCondition(monstrous), "This creature is already monstrous"));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(monstrous,
                new GrantKeywordEffect(Keyword.FLYING, GrantScope.SELF)));
    }
}
