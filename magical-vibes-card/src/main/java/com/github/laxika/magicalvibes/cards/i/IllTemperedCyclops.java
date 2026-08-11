package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceIsMonstrous;
import com.github.laxika.magicalvibes.model.effect.MonstrosityEffect;

import java.util.List;

@CardRegistration(set = "THS", collectorNumber = "125")
public class IllTemperedCyclops extends Card {

    public IllTemperedCyclops() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{5}{R}",
                List.of(new MonstrosityEffect(3)),
                "{5}{R}: Monstrosity 3."
        ).withActivationCondition(new NotCondition(new SourceIsMonstrous()), "This creature is already monstrous"));
    }
}
