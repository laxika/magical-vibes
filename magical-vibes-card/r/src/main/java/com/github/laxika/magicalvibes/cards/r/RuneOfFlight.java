package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordToEquippedCreatureOfEnchantedEquipmentEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "KHM", collectorNumber = "75")
public class RuneOfFlight extends Card {

    public RuneOfFlight() {
        target(TargetFilters.permanent())
                .addTrigger(Triggers.enters(ObjectRelation.THIS), new DrawCardEffect(1))
                .addStaticEffect(new GrantKeywordEffect(
                        Keyword.FLYING, GrantScope.ENCHANTED_PERMANENT, new PermanentIsCreaturePredicate()))
                .addStaticEffect(new GrantKeywordToEquippedCreatureOfEnchantedEquipmentEffect(Keyword.FLYING));
    }
}
