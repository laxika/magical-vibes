package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForBasicLandsToBattlefieldTappedAndHandEffect;

public class ClaimTerritory extends Card {

    public ClaimTerritory() {
        addEffect(EffectSlot.SPELL,
                new SearchLibraryForBasicLandsToBattlefieldTappedAndHandEffect(CardSubtype.FOREST, null));
    }
}
