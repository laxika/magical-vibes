package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "POR", collectorNumber = "117")
public class WickedPact extends Card {

    public WickedPact() {
        // Destroy two target nonblack creatures. You lose 5 life.
        // Two distinct target groups (shared targets not allowed) so the two
        // nonblack creatures must differ, each destroyed by its own effect.
        target(nonblackCreatureFilter("First target must be a nonblack creature"))
                .addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect());
        target(nonblackCreatureFilter("Second target must be a nonblack creature"))
                .addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect());

        addEffect(EffectSlot.SPELL, new LoseLifeEffect(5));
    }

    private static PermanentPredicateTargetFilter nonblackCreatureFilter(String description) {
        return new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentNotPredicate(new PermanentColorInPredicate(Set.of(CardColor.BLACK)))
                )),
                description);
    }
}
