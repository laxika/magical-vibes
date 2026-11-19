package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DFT", collectorNumber = "72")
public class UnstoppablePlan extends Card {

    public UnstoppablePlan() {
        // At the beginning of your end step, untap all nonland permanents you control.
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                new UntapPermanentsEffect(Subjects.controlledBy(Subjects.you(),
                        new PermanentNotPredicate(new PermanentIsLandPredicate()))));
    }
}
