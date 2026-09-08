package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "BNG", collectorNumber = "96")
public class FelhideSpiritbinder extends Card {

    public FelhideSpiritbinder() {
        PermanentPredicateTargetFilter anotherCreature = new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
                )),
                "Target must be another creature"
        );

        target(anotherCreature).addEffect(EffectSlot.ON_SELF_BECOMES_UNTAPPED,
                new MayPayManaEffect("{1}{R}",
                        new CreateTokenCopyOfTargetPermanentEffect(
                                List.of(), Set.of(CardType.ENCHANTMENT), null, null, Map.of(),
                                true, true, false, false),
                        "Pay {1}{R} to create a hasty enchantment token copy of another creature?"));
    }
}
