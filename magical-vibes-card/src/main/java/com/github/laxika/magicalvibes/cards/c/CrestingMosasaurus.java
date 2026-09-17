package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.SacrificePermanentsCost;
import com.github.laxika.magicalvibes.model.condition.WasCast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "REX", collectorNumber = "2")
@CardRegistration(set = "REX", collectorNumber = "28")
public class CrestingMosasaurus extends Card {

    public CrestingMosasaurus() {
        // Emerge {6}{U} — sacrifice a creature and pay the emerge cost reduced by that creature's
        // mana value (generic only; colored components cannot be reduced).
        addCastingOption(new AlternateHandCast(List.of(
                new ManaCastingCost("{6}{U}"),
                new SacrificePermanentsCost(1, new PermanentIsCreaturePredicate())
        ), true));

        // When this creature enters, if you cast it, return each non-Dinosaur creature to its
        // owner's hand.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(new WasCast(),
                ReturnToHandEffect.allPermanentsMatching(new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.DINOSAUR))
                )))));
    }
}
