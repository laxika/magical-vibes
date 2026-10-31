package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardOnDeathThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "YSOS", collectorNumber = "26")
public class ScintillatingEncore extends Card {

    public ScintillatingEncore() {
        target(TargetFilters.creatureYouControl())
                .addSpellEffect(new PerpetuallyBoostTargetCreatureEffect(2, 0))
                .addSpellEffect(new ReturnTargetCardOnDeathThisTurnEffect(true));
    }
}
