package com.github.laxika.magicalvibes.model.filter;

import com.github.laxika.magicalvibes.model.CardSubtype;

/**
 * Matches permanents whose current controller was dealt combat damage this turn by at least {@code minimumSources}
 * different creatures that had {@code subtype} as they dealt it (a changeling counts): "target nonland permanent
 * controlled by a player who was dealt combat damage by three or more Pirates this turn" (Admiral Beckett Brass). A
 * creature that dealt combat damage twice counts once, and it counts whether or not it is still on the battlefield or
 * still has the subtype (the card's rulings).
 *
 * @param subtype        the creature type the damage sources had
 * @param minimumSources how many different such creatures dealt the player combat damage
 */
public record PermanentControllerDealtCombatDamageBySubtypeThisTurnPredicate(CardSubtype subtype, int minimumSources)
        implements PermanentPredicate {
}
