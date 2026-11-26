package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SOM", collectorNumber = "73")
@CardRegistration(set = "FDN", collectorNumber = "179")
@CardRegistration(set = "BRO", collectorNumber = "111")
public class PainfulQuandary extends Card {

    public PainfulQuandary() {
        // Whenever an opponent casts a spell, that player loses 5 life unless they discard a card.
        addTrigger(Triggers.opponentCastsSpell().anySpell(),
                UnlessPaysEffect.unlessPays(Subjects.triggeringPlayer(), new DiscardCardTypeCost(null, null),
                        new LoseLifeEffect(5, Subjects.triggeringPlayer())));
    }
}
