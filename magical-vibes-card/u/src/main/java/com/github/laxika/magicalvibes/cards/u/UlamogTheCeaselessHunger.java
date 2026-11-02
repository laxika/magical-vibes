package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsOfDefendingPlayerLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BFZ", collectorNumber = "15")
@CardRegistration(set = "SLD", collectorNumber = "1122")
@CardRegistration(set = "HA3", collectorNumber = "1")
@CardRegistration(set = "CMM", collectorNumber = "5")
@CardRegistration(set = "CMM", collectorNumber = "454")
@CardRegistration(set = "CMM", collectorNumber = "670")
public class UlamogTheCeaselessHunger extends Card {

    public UlamogTheCeaselessHunger() {
        target(TargetFilters.permanent(), 2, 2)
                .addTrigger(Triggers.youCastThisSpell(), new ExileTargetPermanentEffect());
        addTrigger(Triggers.attacks(), new ExileTopCardsOfDefendingPlayerLibraryEffect(20));
    }
}
