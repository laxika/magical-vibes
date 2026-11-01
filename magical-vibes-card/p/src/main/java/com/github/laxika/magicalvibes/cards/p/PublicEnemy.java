package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreaturesMustAttackEnchantedCreatureControllerEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SNC", collectorNumber = "55")
public class PublicEnemy extends Card {

    public PublicEnemy() {
        target(TargetFilters.creature())
                .addStaticEffect(new CreaturesMustAttackEnchantedCreatureControllerEffect());
        addTrigger(Triggers.dies(ObjectRelation.ENCHANTED), new DrawCardEffect());
    }
}
