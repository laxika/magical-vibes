package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicates;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "HBG", collectorNumber = "194")
public class WrathfulRedDragon extends Card {

    public WrathfulRedDragon() {
        target(new AnyTargetPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        TargetPredicates.anyTarget().permanentRestriction().orElseThrow(),
                        new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.DRAGON)))),
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must not be a Dragon"
        )).addEffect(EffectSlot.ON_ANY_PERMANENT_DEALT_DAMAGE,
                new TriggeringPermanentConditionalEffect(
                        new PermanentHasSubtypePredicate(CardSubtype.DRAGON),
                        new DealDamageToAnyTargetEffect(new XValue())));
    }
}
