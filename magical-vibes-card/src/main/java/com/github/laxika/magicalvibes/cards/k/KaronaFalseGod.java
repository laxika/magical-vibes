package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesOfChosenSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerGainsControlOfSourceCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;

@CardRegistration(set = "SCG", collectorNumber = "138")
public class KaronaFalseGod extends Card {

    public KaronaFalseGod() {
        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED, SequenceEffect.of(
                new UntapPermanentsEffect(TapUntapScope.SOURCE_PERMANENT),
                TargetPlayerGainsControlOfSourceCreatureEffect.triggeringPlayer()));
        addEffect(EffectSlot.ON_ATTACK, new BoostAllCreaturesOfChosenSubtypeEffect(3, 3));
    }
}
