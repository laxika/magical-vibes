package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "LGN", collectorNumber = "94")
@CardRegistration(set = "DD1", collectorNumber = "37")
@CardRegistration(set = "EVG", collectorNumber = "37")
public class GempalmIncinerator extends Card {

    public GempalmIncinerator() {
        addEffect(EffectSlot.ON_SELF_CYCLED, new MayEffect(
                new DealDamageToTargetCreatureEffect(
                        new PermanentCount(new PermanentHasSubtypePredicate(CardSubtype.GOBLIN), CountScope.ANY_PLAYER)),
                "Have Gempalm Incinerator deal damage equal to the number of Goblins on the battlefield?"));
        addCycling("{1}{R}");
    }
}
