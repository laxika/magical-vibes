package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "XLN", collectorNumber = "52")
public class DepthsOfDesire extends Card {

    public DepthsOfDesire() {
        // Return target creature to its owner's hand.
        target(TargetFilters.creature()).addSpellEffect(ReturnToHandEffect.target());

        // Create a Treasure token.
        addSpellEffect(CreateTokenEffect.ofTreasureToken(1));
    }
}
