package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;

/**
 * A floating effect that changes who declares attackers or blockers, or how: its controller chooses the attackers
 * (Master Warcraft), its controller chooses the blockers (Melee, Master Warcraft), or the defending players separate
 * their creatures into piles instead of declaring blockers (Camouflage).
 *
 * @param kind       what the effect replaces
 * @param sourceCard the card whose spell or ability made it, for the prompts the replaced declaration asks
 */
public record DeclarationReplacementEffect(Kind kind, Card sourceCard) implements CardEffect {

    /** What the effect replaces. */
    public enum Kind {
        /** The effect's controller chooses which creatures attack. */
        CONTROLLER_CHOOSES_ATTACKERS,
        /** The effect's controller chooses which creatures block and how they block. */
        CONTROLLER_CHOOSES_BLOCKERS,
        /** Instead of declaring blockers, each defending player separates their creatures into piles. */
        BLOCKERS_IN_PILES
    }
}
