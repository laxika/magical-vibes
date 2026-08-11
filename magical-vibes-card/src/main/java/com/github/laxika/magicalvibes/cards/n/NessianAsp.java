package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceIsMonstrous;
import com.github.laxika.magicalvibes.model.effect.MonstrosityEffect;

import java.util.List;

@CardRegistration(set = "THS", collectorNumber = "164")
public class NessianAsp extends Card {

    public NessianAsp() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{6}{G}",
                List.of(new MonstrosityEffect(4)),
                "{6}{G}: Monstrosity 4."
        ).withActivationCondition(new NotCondition(new SourceIsMonstrous()), "This creature is already monstrous"));
    }
}
