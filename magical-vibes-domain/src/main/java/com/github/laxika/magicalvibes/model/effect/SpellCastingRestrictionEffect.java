package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;

/**
 * A static effect that can prevent a spell from being cast. Restrictions that depend on the
 * candidate spell implement {@link #preventsCasting(Card, Card)}; restrictions that depend on the
 * source permanent's combat state can expose their active scope through the default fact below.
 */
public interface SpellCastingRestrictionEffect extends CardEffect {

    boolean preventsCasting(Card mostRecentSpell, Card candidateSpell);

    /**
     * Whether this effect prevents the player defending against its source from casting spells
     * while that source is attacking.
     */
    default boolean restrictsDefendingPlayerWhileSourceAttacking() {
        return false;
    }
}
