package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostCreatureCardInHandEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "YOTJ", collectorNumber = "1")
public class PrairieSurvivalist extends Card {

    public PrairieSurvivalist() {
        target(TargetFilters.creatureYouControl())
                .addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU),
                        new PerpetuallyBoostTargetCreatureEffect(1, 0))
                .addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU),
                        new PerpetuallyBoostCreatureCardInHandEffect(1, Set.of()));
    }
}
