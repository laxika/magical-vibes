package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

public class DefiantThundermaw extends Card {

    public DefiantThundermaw() {
        addTrigger(Triggers.creatureYouControlAttacks()
                        .withFilter(new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.DRAGON))),
                DealDamageToAnyTargetEffect.dealtByTriggeringPermanent(new Fixed(2)));
    }
}
