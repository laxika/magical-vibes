package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "C19", collectorNumber = "29")
public class TectonicHellion extends Card {

    public TectonicHellion() {
        addEffect(EffectSlot.ON_ATTACK, new SacrificePermanentsEffect(
                2, new PermanentIsLandPredicate(), SacrificeRecipient.PLAYERS_WITH_MOST_LANDS));
    }
}
