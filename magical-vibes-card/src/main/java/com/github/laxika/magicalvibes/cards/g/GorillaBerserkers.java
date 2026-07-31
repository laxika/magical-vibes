package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CreaturesBlockingSource;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedByFewerThanNCreaturesEffect;

@CardRegistration(set = "ALL", collectorNumber = "93a")
@CardRegistration(set = "ALL", collectorNumber = "93b")
public class GorillaBerserkers extends Card {

    public GorillaBerserkers() {
        // Trample is auto-loaded from Scryfall.
        // Rampage 2: whenever this becomes blocked, it gets +2/+2 until end of turn for each
        // creature blocking it beyond the first, i.e. 2 * (blockers - 1).
        addEffect(EffectSlot.ON_BECOMES_BLOCKED, new BoostSelfEffect(
                new Scaled(new Sum(new CreaturesBlockingSource(), new Fixed(-1)), 2),
                new Scaled(new Sum(new CreaturesBlockingSource(), new Fixed(-1)), 2)));

        addEffect(EffectSlot.STATIC, new CantBeBlockedByFewerThanNCreaturesEffect(3));
    }
}
