package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M20", collectorNumber = "147")
public class LavakinBrawler extends Card {

    public LavakinBrawler() {
        // Whenever this creature attacks, it gets +1/+0 until end of turn for each Elemental you control.
        addTrigger(Triggers.attacks(), new BoostSelfEffect(
                new PermanentCount(new PermanentHasSubtypePredicate(CardSubtype.ELEMENTAL), CountScope.CONTROLLER),
                new Fixed(0)));
    }
}
