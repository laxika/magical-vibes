package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayPayManaAndSacrificePermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RevealUntilNonlandPermanentToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "THB", collectorNumber = "130")
@CardRegistration(set = "2X2", collectorNumber = "108")
public class DreamshaperShaman extends Card {

    public DreamshaperShaman() {
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                new MayPayManaAndSacrificePermanentEffect(
                        "{2}{R}",
                        new PermanentNotPredicate(new PermanentIsLandPredicate()),
                        new RevealUntilNonlandPermanentToBattlefieldEffect(),
                        "nonland permanent"));
    }
}
