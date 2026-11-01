package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "USG", collectorNumber = "5")
@CardRegistration(set = "VMA", collectorNumber = "19")
public class BrilliantHalo extends Card {

    public BrilliantHalo() {
        target(TargetFilters.creature())
                .addStaticEffect(new StaticBoostEffect(1, 2, GrantScope.ENCHANTED_CREATURE));
        addTrigger(Triggers.putIntoGraveyardFrom(Zone.BATTLEFIELD, ObjectRelation.THIS),
                ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.HAND)
                        .source(GraveyardSearchScope.ALL_GRAVEYARDS)
                        .filter(new CardIsSelfPredicate())
                        .returnAll(true)
                        .build());
    }
}
