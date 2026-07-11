package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "POR", collectorNumber = "125")
public class FireDragon extends Card {

    public FireDragon() {
        target(new PermanentPredicateTargetFilter(new PermanentIsCreaturePredicate(), "Target must be a creature"))
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new DealDamageToTargetCreatureEffect(new PermanentCount(
                                new PermanentHasSubtypePredicate(CardSubtype.MOUNTAIN), CountScope.CONTROLLER)));
    }
}
