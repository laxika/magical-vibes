package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.p.PackAPunch;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BecomePreparedEffect;

/**
 * Kirol, History Buff // Pack a Punch (SOS 198).
 *
 * <p>Kirol becomes prepared whenever one or more cards leave its controller's graveyard.
 */
@CardRegistration(set = "SOS", collectorNumber = "198")
public class KirolHistoryBuffPackAPunch extends Card {

    public KirolHistoryBuffPackAPunch() {
        setBackFaceCard(new PackAPunch());

        // Whenever one or more cards leave your graveyard, Kirol becomes prepared.
        addEffect(EffectSlot.ON_CONTROLLER_CARDS_LEAVE_GRAVEYARD, new BecomePreparedEffect());
    }

    @Override
    public String getBackFaceClassName() {
        return "PackAPunch";
    }
}
