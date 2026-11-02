package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaUntilEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.effect.PayXManaReanimateCreaturesWithTotalManaValueXEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

public class FireLordSozin extends Card {

    public FireLordSozin() {
        addTrigger(Triggers.attacks(), new AwardManaUntilEndOfCombatEffect(ManaColor.RED, 3));
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new PayXManaReanimateCreaturesWithTotalManaValueXEffect());
    }
}
