package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "PCY", collectorNumber = "52")
public class TroublesomeSpirit extends Card {

    public TroublesomeSpirit() {
        // At the beginning of your end step, tap all lands you control.
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                new TapPermanentsEffect(TapUntapScope.CONTROLLED, new PermanentIsLandPredicate()));
    }
}
