package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LGN", collectorNumber = "94")
@CardRegistration(set = "DD1", collectorNumber = "37")
@CardRegistration(set = "EVG", collectorNumber = "37")
@CardRegistration(set = "DDT", collectorNumber = "39")
@CardRegistration(set = "HA3", collectorNumber = "16")
@CardRegistration(set = "DMR", collectorNumber = "122")
public class GempalmIncinerator extends Card {

    public GempalmIncinerator() {
        addTrigger(Triggers.youCycleThisCard(), new MayEffect(
                new DealDamageToTargetCreatureEffect(
                        new PermanentCount(new PermanentHasSubtypePredicate(CardSubtype.GOBLIN), CountScope.ANY_PLAYER)),
                "Have Gempalm Incinerator deal damage equal to the number of Goblins on the battlefield?"));
        addCycling("{1}{R}");
    }
}
