package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.condition.Delirium;
import com.github.laxika.magicalvibes.model.effect.EachOpponentMayDiscardOrSacrificePermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DSK", collectorNumber = "112")
public class OsseousSticktwister extends Card {

    public OsseousSticktwister() {
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(new Delirium()),
                new EachOpponentMayDiscardOrSacrificePermanentEffect(
                        new SourcePower(), new PermanentNotPredicate(new PermanentIsLandPredicate())));
    }
}
