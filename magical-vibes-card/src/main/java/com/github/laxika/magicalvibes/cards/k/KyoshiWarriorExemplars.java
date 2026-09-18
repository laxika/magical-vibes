package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "TLE", collectorNumber = "140")
public class KyoshiWarriorExemplars extends Card {

    public KyoshiWarriorExemplars() {
        addEffect(EffectSlot.ON_ATTACK, new ConditionalEffect(
                new ControlsPermanentCount(8, new PermanentIsLandPredicate()),
                new BoostAllOwnCreaturesEffect(2, 2)));
    }
}
