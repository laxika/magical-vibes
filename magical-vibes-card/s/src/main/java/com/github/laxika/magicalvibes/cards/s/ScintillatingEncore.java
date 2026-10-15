package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardOnDeathThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "YSOS", collectorNumber = "26")
public class ScintillatingEncore extends Card {

    public ScintillatingEncore() {
        target(TargetFilters.creatureYouControl())
                .addEffect(EffectSlot.SPELL, new PerpetuallyBoostTargetCreatureEffect(2, 0))
                .addEffect(EffectSlot.SPELL, new ReturnTargetCardOnDeathThisTurnEffect(true));
    }
}
