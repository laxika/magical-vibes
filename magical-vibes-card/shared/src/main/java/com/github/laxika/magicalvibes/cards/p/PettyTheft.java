package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

public class PettyTheft extends Card {

    public PettyTheft() {
        target(TargetFilters.nonlandPermanentAnOpponentControls())
                .addSpellEffect(ReturnToHandEffect.target());
    }
}
