package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LGN", collectorNumber = "85")
public class VileDeacon extends Card {

    public VileDeacon() {
        PermanentCount clericsOnBattlefield = new PermanentCount(
                new PermanentHasSubtypePredicate(CardSubtype.CLERIC), CountScope.ANY_PLAYER);
        addTrigger(Triggers.attacks(), new BoostSelfEffect(clericsOnBattlefield, clericsOnBattlefield));
    }
}
