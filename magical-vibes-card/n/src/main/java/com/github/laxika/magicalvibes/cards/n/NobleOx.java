package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CanBlockAnyNumberOfCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.MakeAllUnblockedCreaturesAttackingControllerBlockedBySourceEffect;

@CardRegistration(set = "MB2", collectorNumber = "285")
@CardRegistration(set = "MB2", collectorNumber = "521")
public class NobleOx extends Card {

    public NobleOx() {
        addEffect(EffectSlot.STATIC, new CanBlockAnyNumberOfCreaturesEffect());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new MakeAllUnblockedCreaturesAttackingControllerBlockedBySourceEffect());
    }
}
