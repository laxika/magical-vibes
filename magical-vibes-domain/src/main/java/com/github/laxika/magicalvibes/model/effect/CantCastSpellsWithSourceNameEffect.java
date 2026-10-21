package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;

/** Prevents casting spells whose name matches the source permanent's current card name. */
public record CantCastSpellsWithSourceNameEffect() implements SpellCastingRestrictionEffect {

    @Override
    public boolean preventsCasting(Card mostRecentSpell, Card candidateSpell) {
        return false;
    }

    @Override
    public boolean preventsCasting(Permanent source, Card mostRecentSpell, Card candidateSpell) {
        // CR 708.2: a face-down permanent has no name, so its name-based restriction doesn't exist.
        return source != null && !source.isFaceDown() && candidateSpell != null
                && source.getCard().getName().equals(candidateSpell.getName());
    }
}
