package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.condition.TargetPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

/**
 * Icequake — {1}{B}{B} Sorcery.
 * Destroy target land. If that land was a snow land, Icequake deals 1 damage to that land's controller.
 */
@CardRegistration(set = "ICE", collectorNumber = "134")
@CardRegistration(set = "ME2", collectorNumber = "94")
public class Icequake extends Card {

    public Icequake() {
        // The snow check and controller read the destroyed land's last-known information, and the
        // damage is dealt afterwards so a destroyed snow Glacial Chasm no longer prevents it.
        target(TargetFilters.land())
                .addSpellEffect(new DestroyTargetPermanentEffect())
                        .addSpellEffect(new ConditionalEffect(new TargetPermanentMatches(new PermanentHasSupertypePredicate(CardSupertype.SNOW)), new DealDamageToPlayersEffect(1, Subjects.controllerOf(Subjects.target()))));
    }
}
