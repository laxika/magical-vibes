package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.MinimumAttackers;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentsPermanentsCantBeTurnedFaceUpEffect;

@CardRegistration(set = "MKM", collectorNumber = "20")
public class KarlovWatchdog extends Card {

    public KarlovWatchdog() {
        addEffect(EffectSlot.STATIC, new OpponentsPermanentsCantBeTurnedFaceUpEffect());
        addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK,
                new ConditionalEffect(new MinimumAttackers(3), new BoostAllOwnCreaturesEffect(1, 1)));
    }
}
