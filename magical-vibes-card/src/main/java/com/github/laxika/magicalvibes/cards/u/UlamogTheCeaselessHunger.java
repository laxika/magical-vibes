package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsOfDefendingPlayerLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "BFZ", collectorNumber = "15")
@CardRegistration(set = "SLD", collectorNumber = "1122")
@CardRegistration(set = "HA3", collectorNumber = "1")
@CardRegistration(set = "CMM", collectorNumber = "5")
@CardRegistration(set = "CMM", collectorNumber = "454")
@CardRegistration(set = "CMM", collectorNumber = "670")
public class UlamogTheCeaselessHunger extends Card {

    public UlamogTheCeaselessHunger() {
        target(TargetFilters.permanent(), 2, 2)
                .addEffect(EffectSlot.ON_SELF_CAST, new ExileTargetPermanentEffect());
        addEffect(EffectSlot.ON_ATTACK, new ExileTopCardsOfDefendingPlayerLibraryEffect(20));
    }
}
