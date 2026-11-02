package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.GreatestPowerAmongControlled;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsForAmountEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DSC", collectorNumber = "35")
@CardRegistration(set = "DSC", collectorNumber = "62")
public class ShriekwoodDevourer extends Card {

    public ShriekwoodDevourer() {
        addTrigger(Triggers.youAttack(),
                new UntapPermanentsForAmountEffect(
                        new GreatestPowerAmongControlled(null, true, true),
                        new PermanentIsLandPredicate(), true));
    }
}
