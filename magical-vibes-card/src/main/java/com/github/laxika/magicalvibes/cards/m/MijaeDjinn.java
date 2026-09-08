package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.FlipCoinWinEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveSourceFromCombatEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;

@CardRegistration(set = "SUM", collectorNumber = "166")
public class MijaeDjinn extends Card {

    public MijaeDjinn() {
        addEffect(EffectSlot.ON_ATTACK, new FlipCoinWinEffect(null,
                SequenceEffect.of(
                        new RemoveSourceFromCombatEffect(),
                        new TapPermanentsEffect(TapUntapScope.SELF))));
    }
}
