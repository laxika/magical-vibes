package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SPE", collectorNumber = "16")
public class RampagingClassmate extends Card {

    public RampagingClassmate() {
        PermanentCount otherAttackingCreatures = new PermanentCount(
                new PermanentIsAttackingPredicate(), CountScope.ANY_PLAYER, true);
        addTrigger(Triggers.attacks(), new BoostSelfEffect(otherAttackingCreatures, new Fixed(0)));
    }
}
