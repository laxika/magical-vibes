package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfEnteringPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "CMM", collectorNumber = "723")
@CardRegistration(set = "CMM", collectorNumber = "756")
@CardRegistration(set = "DSC", collectorNumber = "101")
public class OnduSpiritdancer extends Card {

    public OnduSpiritdancer() {
        // Whenever an enchantment you control enters, you may create a token that's a copy of it.
        // Do this only once each turn; only accepting the copy uses up the turn (ruling 2023-07-28).
        addTrigger(Triggers.enters(Triggers.ENCHANTMENT, ObjectRelation.YOU_CONTROL),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.DO_ONLY_ONCE_EACH_TURN),
                new MayEffect(
                        new CreateTokenCopyOfEnteringPermanentEffect(),
                        "Create a token that's a copy of that enchantment?"));
    }
}
