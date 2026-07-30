package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect.ChooseOneOption;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import java.util.List;

/**
 * "Choose an order for artifacts, creatures, and lands. Each player sacrifices one permanent of
 * their choice of the first type, sacrifices two of their choice of the second type, then
 * sacrifices three of their choice of the third type."
 *
 * <p>The order choice is modelled as a modal spell with one mode per permutation of the three
 * types; each mode splices three {@code EACH_PLAYER} forced sacrifices (1 / 2 / 3) into the
 * resolution in the chosen order. Each sacrifice step is its own APNAP choice round, so a player
 * who loses their artifacts to step one may not use them for a later step.
 */
@CardRegistration(set = "AVR", collectorNumber = "153")
public class RiteOfRuin extends Card {

    private static final PermanentPredicate ARTIFACTS = new PermanentIsArtifactPredicate();
    // Wrapped: a bare PermanentIsCreaturePredicate routes SacrificePermanentsEffect through the
    // single-creature primitive, which would ignore the 2- and 3-permanent counts.
    private static final PermanentPredicate CREATURES =
            new PermanentAllOfPredicate(List.of(new PermanentIsCreaturePredicate()));
    private static final PermanentPredicate LANDS = new PermanentIsLandPredicate();

    public RiteOfRuin() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                order("Artifacts, then creatures, then lands", ARTIFACTS, CREATURES, LANDS),
                order("Artifacts, then lands, then creatures", ARTIFACTS, LANDS, CREATURES),
                order("Creatures, then artifacts, then lands", CREATURES, ARTIFACTS, LANDS),
                order("Creatures, then lands, then artifacts", CREATURES, LANDS, ARTIFACTS),
                order("Lands, then artifacts, then creatures", LANDS, ARTIFACTS, CREATURES),
                order("Lands, then creatures, then artifacts", LANDS, CREATURES, ARTIFACTS))));
    }

    private static ChooseOneOption order(String label, PermanentPredicate first,
            PermanentPredicate second, PermanentPredicate third) {
        return new ChooseOneOption(label, List.<CardEffect>of(
                new SacrificePermanentsEffect(1, first, SacrificeRecipient.EACH_PLAYER),
                new SacrificePermanentsEffect(2, second, SacrificeRecipient.EACH_PLAYER),
                new SacrificePermanentsEffect(3, third, SacrificeRecipient.EACH_PLAYER)));
    }
}
