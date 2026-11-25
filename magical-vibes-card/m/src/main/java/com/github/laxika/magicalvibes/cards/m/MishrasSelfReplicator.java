package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSourceEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsHistoricPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DOM", collectorNumber = "223")
public class MishrasSelfReplicator extends Card {

    public MishrasSelfReplicator() {
        // Whenever you cast a historic spell, you may pay {1}. If you do, create a token
        // that's a copy of Mishra's Self-Replicator. (Artifacts, legendaries, and Sagas are historic.)
        addTrigger(Triggers.youCastSpell().withFilter(new CardIsHistoricPredicate()),
                UnlessPaysEffect.youMayPay("{1}", new CreateTokenCopyOfSourceEffect())
                        .withPrompt("Pay {1} to create a token that's a copy of Mishra's Self-Replicator?"));
    }
}
