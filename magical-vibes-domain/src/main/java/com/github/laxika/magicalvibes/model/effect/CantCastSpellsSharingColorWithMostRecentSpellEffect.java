package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;

/**
 * Static effect: players can't cast a spell that shares a color with the most recently cast spell
 * this turn. Colorless spells have no colors to share.
 */
public record CantCastSpellsSharingColorWithMostRecentSpellEffect()
        implements SpellCastingRestrictionEffect {

    @Override
    public boolean preventsCasting(Card mostRecentSpell, Card candidateSpell) {
        if (mostRecentSpell == null || candidateSpell == null
                || mostRecentSpell.getColors() == null || candidateSpell.getColors() == null) {
            return false;
        }
        return mostRecentSpell.getColors().stream().anyMatch(candidateSpell.getColors()::contains);
    }
}
