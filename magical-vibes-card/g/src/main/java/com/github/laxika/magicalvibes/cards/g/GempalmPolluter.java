package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LGN", collectorNumber = "70")
@CardRegistration(set = "HA3", collectorNumber = "11")
@CardRegistration(set = "DRC", collectorNumber = "93")
public class GempalmPolluter extends Card {

    public GempalmPolluter() {
        addTrigger(Triggers.youCycleThisCard(), new MayEffect(
                new LoseLifeEffect(
                        new PermanentCount(new PermanentHasSubtypePredicate(CardSubtype.ZOMBIE), CountScope.ANY_PLAYER),
                        LoseLifeRecipient.TARGET_PLAYER),
                "Have target player lose life equal to the number of Zombies on the battlefield?"));
        addCycling("{B}{B}");
    }
}
