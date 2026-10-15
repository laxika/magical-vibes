package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;

/**
 * Rags // Riches — front half (Rags).
 * Sorcery — All creatures get -2/-2 until end of turn.
 * Back half (Riches) is cast only from the graveyard via Aftermath (FlashbackCast on the back face).
 */
@CardRegistration(set = "AKH", collectorNumber = "222")
@CardRegistration(set = "AKR", collectorNumber = "252")
public class RagsRiches extends Card {

    public RagsRiches() {
        setBackFaceCard(new Riches());

        // All creatures get -2/-2 until end of turn.
        addEffect(EffectSlot.SPELL, new BoostAllCreaturesEffect(-2, -2));
    }

    @Override
    public String getBackFaceClassName() {
        return "Riches";
    }
}
