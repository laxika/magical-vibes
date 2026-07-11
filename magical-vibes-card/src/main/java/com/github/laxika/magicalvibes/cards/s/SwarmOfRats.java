package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "9ED", collectorNumber = "166")
@CardRegistration(set = "P02", collectorNumber = "89")
public class SwarmOfRats extends Card {

    public SwarmOfRats() {
        // Swarm of Rats's power (printed *) is equal to the number of Rats you control, including itself.
        addEffect(EffectSlot.STATIC, new BoostSelfEffect(
                new PermanentCount(new PermanentHasSubtypePredicate(CardSubtype.RAT), CountScope.CONTROLLER),
                new Fixed(0)));
    }
}
