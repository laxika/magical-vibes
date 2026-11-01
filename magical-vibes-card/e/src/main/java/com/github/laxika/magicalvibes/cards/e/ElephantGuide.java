package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "JUD", collectorNumber = "111")
@CardRegistration(set = "DDD", collectorNumber = "18")
@CardRegistration(set = "VMA", collectorNumber = "205")
@CardRegistration(set = "GVL", collectorNumber = "18")
@CardRegistration(set = "EMA", collectorNumber = "163")
public class ElephantGuide extends Card {

    public ElephantGuide() {
        target(TargetFilters.creature())
                .addStaticEffect(new StaticBoostEffect(3, 3, GrantScope.ENCHANTED_CREATURE))
                .addTrigger(Triggers.dies(ObjectRelation.ENCHANTED),
                        new CreateTokenEffect("Elephant", 3, 3, CardColor.GREEN,
                                List.of(CardSubtype.ELEPHANT), Set.of(), Set.of()));
    }
}
