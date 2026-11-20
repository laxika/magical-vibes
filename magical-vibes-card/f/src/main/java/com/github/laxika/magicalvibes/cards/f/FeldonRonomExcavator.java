package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BRO", collectorNumber = "135")
public class FeldonRonomExcavator extends Card {

    public FeldonRonomExcavator() {
        addStaticEffect(new CantBlockEffect());
        addTrigger(Triggers.dealtDamage(),
                new ExileTopCardMayPlayThisTurnEffect(new EventValue(), false)
                        .until(Duration.UNTIL_END_OF_YOUR_NEXT_TURN).choosingOne());
    }
}
