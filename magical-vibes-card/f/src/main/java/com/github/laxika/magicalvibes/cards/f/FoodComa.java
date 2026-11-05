package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WOE", collectorNumber = "308")
public class FoodComa extends Card {

    public FoodComa() {
        var exileTargetPermanentUntilSourceLeaves = new ExileTargetPermanentUntilSourceLeavesEffect();
        target(TargetFilters.creatureAnOpponentControls())
                .bindEffect(exileTargetPermanentUntilSourceLeaves);
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                exileTargetPermanentUntilSourceLeaves,
                CreateTokenEffect.ofFoodToken(1));
    }
}
