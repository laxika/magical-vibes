package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantSupertypeToOwnLandsAndLandCardsEffect;

@CardRegistration(set = "BRC", collectorNumber = "24")
@CardRegistration(set = "BRC", collectorNumber = "44")
public class RootpathPurifier extends Card {

    public RootpathPurifier() {
        addEffect(EffectSlot.STATIC,
                new GrantSupertypeToOwnLandsAndLandCardsEffect(CardSupertype.BASIC));
    }
}
