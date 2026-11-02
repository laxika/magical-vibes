package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayPlayCardsFromTopOfLibraryForLifeUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SPM", collectorNumber = "56")
@CardRegistration(set = "SPM", collectorNumber = "256")
@CardRegistration(set = "SPM", collectorNumber = "286")
@CardRegistration(set = "OM1", collectorNumber = "56")
public class GwenomRemorseless extends Card {

    public GwenomRemorseless() {
        addTrigger(Triggers.attacks(), new MayPlayCardsFromTopOfLibraryForLifeUntilEndOfTurnEffect());
    }
}
