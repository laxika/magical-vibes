package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

@CardRegistration(set = "FIN", collectorNumber = "206")
@CardRegistration(set = "FIN", collectorNumber = "391")
@CardRegistration(set = "FIN", collectorNumber = "473")
@CardRegistration(set = "FIN", collectorNumber = "536")
@CardRegistration(set = "FIN", collectorNumber = "567")
public class TifaLockhart extends Card {

    public TifaLockhart() {
        addEffect(EffectSlot.ON_ALLY_LAND_ENTERS_BATTLEFIELD,
                new BoostSelfEffect(new SourcePower(), new Fixed(0)));
    }
}
