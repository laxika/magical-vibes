package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.PlaysAdditionalLandEachTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnPermanentControlledByPlayerToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "FIN", collectorNumber = "170")
public class ZellDincht extends Card {

    public ZellDincht() {
        addEffect(EffectSlot.STATIC, new PlaysAdditionalLandEachTurnEffect(1));

        PermanentCount landsYouControl =
                new PermanentCount(new PermanentIsLandPredicate(), CountScope.CONTROLLER);
        addEffect(EffectSlot.STATIC, new BoostSelfEffect(landsYouControl, new Fixed(0)));

        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new ReturnPermanentControlledByPlayerToHandEffect(new PermanentIsLandPredicate(), "land"));
    }
}
