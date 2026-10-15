package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicates;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "LCC", collectorNumber = "56")
@CardRegistration(set = "LCC", collectorNumber = "88")
public class WrathfulRaptors extends Card {

    public WrathfulRaptors() {
        // Whenever a Dinosaur you control is dealt damage, it deals that much damage to any target
        // that isn't a Dinosaur.
        target(new AnyTargetPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        TargetPredicates.anyTarget().permanentRestriction().orElseThrow(),
                        new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.DINOSAUR))
                )),
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must not be a Dinosaur"
        )).addEffect(EffectSlot.ON_ANY_PERMANENT_DEALT_DAMAGE,
                new TriggeringPermanentConditionalEffect(
                        new PermanentHasSubtypePredicate(CardSubtype.DINOSAUR),
                        new DealDamageToAnyTargetEffect(new XValue())));
    }
}
