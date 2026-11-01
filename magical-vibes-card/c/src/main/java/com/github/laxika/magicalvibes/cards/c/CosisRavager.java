package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WWK", collectorNumber = "77")
@CardRegistration(set = "DDI", collectorNumber = "52")
public class CosisRavager extends Card {

    public CosisRavager() {
        // Landfall — Whenever a land you control enters, you may have this creature deal 1 damage
        // to target player or planeswalker.
        target(new AnyTargetPredicateTargetFilter(
                new PermanentIsPlaneswalkerPredicate(),
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player or planeswalker"))
                .addTrigger(Triggers.enters(Triggers.LAND, ObjectRelation.YOU_CONTROL), new MayEffect(
                        new DealDamageToTargetPlayerOrPlaneswalkerEffect(1),
                        "Deal 1 damage to target player or planeswalker?"));
    }
}
