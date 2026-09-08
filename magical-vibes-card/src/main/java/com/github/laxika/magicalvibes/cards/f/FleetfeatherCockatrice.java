package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceIsMonstrous;
import com.github.laxika.magicalvibes.model.effect.MonstrosityEffect;

import java.util.List;

@CardRegistration(set = "JOU", collectorNumber = "149")
public class FleetfeatherCockatrice extends Card {

    public FleetfeatherCockatrice() {
        addActivatedAbility(new ActivatedAbility(false, "{5}{G}{U}", List.of(new MonstrosityEffect(3)),
                "{5}{G}{U}: Monstrosity 3.")
                .withActivationCondition(new NotCondition(new SourceIsMonstrous()),
                        "This creature is already monstrous"));
    }
}
