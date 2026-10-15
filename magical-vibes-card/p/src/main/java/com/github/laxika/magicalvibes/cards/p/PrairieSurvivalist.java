package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostCreatureCardInHandEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.Set;

@CardRegistration(set = "YOTJ", collectorNumber = "1")
public class PrairieSurvivalist extends Card {

    public PrairieSurvivalist() {
        target(TargetFilters.creatureYouControl())
                .addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED,
                        new PerpetuallyBoostTargetCreatureEffect(1, 0))
                .addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED,
                        new PerpetuallyBoostCreatureCardInHandEffect(1, Set.of()));
    }
}
