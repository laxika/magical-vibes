package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.EventStat;
import com.github.laxika.magicalvibes.model.effect.ThenEffectRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

/**
 * Filigree Fracture — {2}{G} Instant.
 * Destroy target artifact or enchantment. If that permanent was blue or black, draw a card.
 */
@CardRegistration(set = "CON", collectorNumber = "82")
public class FiligreeFracture extends Card {

    public FiligreeFracture() {
        // The colour check uses the destroyed permanent's last-known colour, and the draw happens
        // only after the destroy, so a Thought Reflection-style draw replacement no longer applies.
        target(new PermanentPredicateTargetFilter(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsArtifactPredicate(),
                        new PermanentIsEnchantmentPredicate()
                )),
                "Target must be an artifact or enchantment."
        ))
                .addSpellEffect(new DestroyTargetPermanentThenEffect(
                        EventStat.NONE,
                        new DrawCardEffect(1),
                        ThenEffectRecipient.CONTROLLER,
                        new PermanentColorInPredicate(Set.of(CardColor.BLUE, CardColor.BLACK))));
    }
}
