package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileDiscardedCardFromGraveyardMayPlayUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MSH", collectorNumber = "107")
public class MoonstoneHarshMistress extends Card {

    public MoonstoneHarshMistress() {
        addTrigger(Triggers.youDiscardCard(), new MayEffect(
                new ExileDiscardedCardFromGraveyardMayPlayUntilNextTurnEffect(),
                "Exile that card from your graveyard?"));
    }
}
