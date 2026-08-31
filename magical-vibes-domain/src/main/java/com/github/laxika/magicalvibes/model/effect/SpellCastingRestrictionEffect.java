package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;

/**
 * A static effect that can prevent a spell from being cast based on cast-time card information.
 */
public interface SpellCastingRestrictionEffect extends CardEffect {

    boolean preventsCasting(Card mostRecentSpell, Card candidateSpell);

    default boolean preventsCasting(Permanent source, Card mostRecentSpell, Card candidateSpell) {
        return preventsCasting(mostRecentSpell, candidateSpell);
    }

    default boolean restrictsDefendingPlayerWhileSourceAttacking() {
        return false;
    }
}
