package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.TotalPowerOfControlledCreatures;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LTC", collectorNumber = "511")
@CardRegistration(set = "LTC", collectorNumber = "555")
public class WitchKingSkyScourge extends Card {

    public WitchKingSkyScourge() {
        addTrigger(Triggers.youAttack().withFilter(new PermanentHasSubtypePredicate(CardSubtype.WRAITH)),
                new ExileTopCardsMayPlayThisTurnEffect(TotalPowerOfControlledCreatures.declaredAttackers(null)));
    }
}
