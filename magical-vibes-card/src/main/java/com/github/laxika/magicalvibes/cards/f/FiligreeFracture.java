package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.TargetPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
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
        // The draw checks the target's colour, so it runs while the permanent is still on the
        // battlefield — before the destroy. Colour doesn't change when it's destroyed, so this is
        // equivalent to the printed "was blue or black" wording.
        target(new PermanentPredicateTargetFilter(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsArtifactPredicate(),
                        new PermanentIsEnchantmentPredicate()
                )),
                "Target must be an artifact or enchantment."
        ))
                .addEffect(EffectSlot.SPELL, new ConditionalEffect(
                        new TargetPermanentMatches(
                                new PermanentColorInPredicate(Set.of(CardColor.BLUE, CardColor.BLACK))),
                        new DrawCardEffect(1)))
                .addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect());
    }
}
