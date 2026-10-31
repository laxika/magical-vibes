package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
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
@CardRegistration(set = "S99", collectorNumber = "92")
@CardRegistration(set = "ME4", collectorNumber = "102")
public class WickedPact extends Card {

    public WickedPact() {
        // Destroy two target nonblack creatures. You lose 5 life.
        // Two distinct target groups (shared targets not allowed) so the two nonblack creatures must
        // differ. One effect bound to both groups destroys them at the same time.
        DestroyTargetPermanentEffect destroy = new DestroyTargetPermanentEffect();
        target(nonblackCreatureFilter("First target must be a nonblack creature"))
                .addSpellEffect(destroy);
        target(nonblackCreatureFilter("Second target must be a nonblack creature")).bindEffect(destroy);

        addSpellEffect(new LoseLifeEffect(5));
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
