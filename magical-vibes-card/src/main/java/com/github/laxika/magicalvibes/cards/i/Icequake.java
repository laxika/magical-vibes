package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.TargetPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

/**
 * Icequake — {1}{B}{B} Sorcery.
 * Destroy target land. If that land was a snow land, Icequake deals 1 damage to that land's controller.
 */
@CardRegistration(set = "ICE", collectorNumber = "134")
public class Icequake extends Card {

    public Icequake() {
        // Snow check runs while the land is still on the battlefield — before the destroy.
        // Equivalent to the printed "was a snow land" wording (Gloomlance pattern).
        target(new PermanentPredicateTargetFilter(
                new PermanentIsLandPredicate(),
                "Target must be a land"
        ))
                .addEffect(EffectSlot.SPELL, new ConditionalEffect(
                        new TargetPermanentMatches(
                                new PermanentHasSupertypePredicate(CardSupertype.SNOW)),
                        new DealDamageToPlayersEffect(1, DamageRecipient.TARGET_PERMANENT_CONTROLLER)))
                .addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect());
    }
}
