package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SpliceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "BOK", collectorNumber = "70")
public class HorobisWhisper extends Card {

    public HorobisWhisper() {
        // If you control a Swamp, destroy target nonblack creature. The target is chosen at cast
        // time regardless of the Swamp; the condition is only checked as the spell resolves.
        target(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentNotPredicate(new PermanentColorInPredicate(Set.of(CardColor.BLACK)))
                )),
                "Target must be a nonblack creature"
        ))
                .addEffect(EffectSlot.SPELL, new ConditionalEffect(
                        new ControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.SWAMP)),
                        new DestroyTargetPermanentEffect()));

        // Splice onto Arcane—Exile four cards from your graveyard.
        addEffect(EffectSlot.STATIC, SpliceEffect.exilingGraveyard(CardSubtype.ARCANE, 4));
    }
}
