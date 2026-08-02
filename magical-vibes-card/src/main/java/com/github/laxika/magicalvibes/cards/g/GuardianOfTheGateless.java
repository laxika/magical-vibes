package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.amount.CreaturesBlockedBySource;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CanBlockAnyNumberOfCreaturesEffect;

@CardRegistration(set = "GTC", collectorNumber = "14")
public class GuardianOfTheGateless extends Card {

    public GuardianOfTheGateless() {
        addEffect(EffectSlot.STATIC, new CanBlockAnyNumberOfCreaturesEffect());
        addEffect(EffectSlot.ON_BLOCK, new BoostSelfEffect(
                new CreaturesBlockedBySource(), new CreaturesBlockedBySource()), TriggerMode.ONCE_PER_BLOCK);
    }
}
