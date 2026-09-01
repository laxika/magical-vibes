package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;

/**
 * Static effect: while the source permanent is attacking, the player it is attacking can't cast
 * spells. When the source attacks a planeswalker, that planeswalker's controller is the defending
 * player.
 */
public record DefendingPlayerCantCastSpellsWhileAttackingEffect()
        implements SpellCastingRestrictionEffect {

    @Override
    public boolean preventsCasting(Card mostRecentSpell, Card candidateSpell) {
        return false;
    }

    @Override
    public boolean restrictsDefendingPlayerWhileSourceAttacking() {
        return true;
    }
}
