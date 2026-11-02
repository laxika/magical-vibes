package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CanBlockAnyNumberOfCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.MakeAllUnblockedCreaturesAttackingControllerBlockedBySourceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MB2", collectorNumber = "285")
@CardRegistration(set = "MB2", collectorNumber = "521")
public class NobleOx extends Card {

    public NobleOx() {
        addStaticEffect(new CanBlockAnyNumberOfCreaturesEffect());
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new MakeAllUnblockedCreaturesAttackingControllerBlockedBySourceEffect());
    }
}
