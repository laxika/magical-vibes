package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSourceEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsHistoricPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "DOM", collectorNumber = "223")
public class MishrasSelfReplicator extends Card {

    public MishrasSelfReplicator() {
        // Whenever you cast a historic spell, you may pay {1}. If you do, create a token
        // that's a copy of Mishra's Self-Replicator. (Artifacts, legendaries, and Sagas are historic.)
        addTrigger(Triggers.youCastSpell(), new MayEffect(
                new SpellCastTriggerEffect(
                        new CardIsHistoricPredicate(),
                        List.of(new CreateTokenCopyOfSourceEffect()),
                        "{1}"),
                "Pay {1} to create a token that's a copy of Mishra's Self-Replicator?"
        ));
    }
}
