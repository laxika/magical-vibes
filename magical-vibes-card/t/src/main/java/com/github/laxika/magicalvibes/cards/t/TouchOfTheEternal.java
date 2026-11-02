package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.SetLifeTotalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M13", collectorNumber = "37")
public class TouchOfTheEternal extends Card {

    public TouchOfTheEternal() {
        // At the beginning of your upkeep, count the number of permanents you control.
        // Your life total becomes that number.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new SetLifeTotalEffect(
                new PermanentCount(new PermanentTruePredicate(), CountScope.CONTROLLER)));
    }
}
