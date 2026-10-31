package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

public class SteamClean extends Card {

    public SteamClean() {
        target(TargetFilters.nonlandPermanent())
                .addSpellEffect(ReturnToHandEffect.target());
    }
}
