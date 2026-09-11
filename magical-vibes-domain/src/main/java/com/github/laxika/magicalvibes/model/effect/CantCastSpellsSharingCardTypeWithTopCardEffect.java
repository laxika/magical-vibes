package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;

/** Static effect: players can't cast spells that share a card type with a top library card. */
public record CantCastSpellsSharingCardTypeWithTopCardEffect()
        implements SpellCastingRestrictionEffect {

    @Override
    public boolean preventsCasting(Card mostRecentSpell, Card candidateSpell) {
        return false;
    }

    @Override
    public boolean preventsCastingFromTopOfLibrary(Card topCard, Card candidateSpell) {
        if (topCard == null || candidateSpell == null) {
            return false;
        }
        for (CardType type : CardType.values()) {
            if (topCard.hasType(type) && candidateSpell.hasType(type)) {
                return true;
            }
        }
        return false;
    }
}
