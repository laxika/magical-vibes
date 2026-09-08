package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileDiscardedCardFromGraveyardMayPlayUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "MSH", collectorNumber = "107")
public class MoonstoneHarshMistress extends Card {

    public MoonstoneHarshMistress() {
        addEffect(EffectSlot.ON_CONTROLLER_DISCARDS, new MayEffect(
                new ExileDiscardedCardFromGraveyardMayPlayUntilNextTurnEffect(),
                "Exile that card from your graveyard?"));
    }
}
