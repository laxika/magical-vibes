package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.EventStat;
import com.github.laxika.magicalvibes.model.effect.ThenEffectRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.Set;

/**
 * Gloomlance — {3}{B}{B} Sorcery.
 * Destroy target creature. If that creature was green or white, its controller discards a card.
 */
@CardRegistration(set = "SHM", collectorNumber = "67")
public class Gloomlance extends Card {

    public Gloomlance() {
        // "Destroy target creature. If that creature was green or white, its controller discards a
        // card." The colour check reads the destroyed creature's last-known information, and "its
        // controller" is snapshotted before the destruction.
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature."
        ))
                .addSpellEffect(new DestroyTargetPermanentThenEffect(
                        EventStat.NONE,
                        new DiscardEffect(1, DiscardRecipient.CONTROLLER),
                        ThenEffectRecipient.TARGET_CONTROLLER,
                        new PermanentColorInPredicate(Set.of(CardColor.GREEN, CardColor.WHITE))));
    }
}
