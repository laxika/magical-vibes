package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.Permanent;

/** Static restriction used by Sanctum Prelate. */
public record NoncreatureSpellsWithChosenManaValueCantBeCastEffect()
        implements SpellCastingRestrictionEffect {

    @Override
    public boolean preventsCasting(Card mostRecentSpell, Card candidateSpell) {
        return false;
    }

    @Override
    public boolean preventsCasting(Permanent source, Card mostRecentSpell, Card candidateSpell) {
        return preventsCasting(source, mostRecentSpell, candidateSpell, null);
    }

    @Override
    public boolean preventsCasting(Permanent source, Card mostRecentSpell, Card candidateSpell,
                                   Integer chosenX) {
        if (source == null || candidateSpell == null
                || candidateSpell.hasType(CardType.CREATURE)
                || candidateSpell.hasType(CardType.LAND)) {
            return false;
        }

        ManaCost manaCost = candidateSpell.getParsedManaCost();
        if (manaCost != null && manaCost.hasX()) {
            if (chosenX == null) {
                return false;
            }
            return candidateSpell.getManaValue()
                    + chosenX * Math.max(1, manaCost.getXSymbolCount())
                    == source.getChosenNumber();
        }
        return candidateSpell.getManaValue() == source.getChosenNumber();
    }
}
