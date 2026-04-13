package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsCreatureSharingTypeWithEnchantedToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "DKA", collectorNumber = "30")
public class CallToTheKindred extends Card {

    public CallToTheKindred() {
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature"
        ))
        .addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new MayEffect(
                        new LookAtTopCardsCreatureSharingTypeWithEnchantedToBattlefieldEffect(5),
                        "Look at the top five cards of your library?"
                ));
    }
}
