package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatedPermanentsAtEndStepEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

public class FurnaceBlessedConqueror extends Card {

    public FurnaceBlessedConqueror() {
        addTrigger(Triggers.attacks(),
                CreateTokenCopyOfSourceEffect.tappedAndAttackingWithSourceCounters(),
                new SacrificeCreatedPermanentsAtEndStepEffect());
    }
}
