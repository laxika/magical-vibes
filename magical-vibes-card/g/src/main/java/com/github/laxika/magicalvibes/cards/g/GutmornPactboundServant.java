package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ConjureDuplicateOfDiscardedCardIntoChosenPlayerHandEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDiscardsNonlandCardEffect;

@CardRegistration(set = "YMID", collectorNumber = "28")
public class GutmornPactboundServant extends Card {

    public GutmornPactboundServant() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new EachPlayerDiscardsNonlandCardEffect());
        addEffect(EffectSlot.ON_CONTROLLER_DISCARDS,
                new ConjureDuplicateOfDiscardedCardIntoChosenPlayerHandEffect());
        addEffect(EffectSlot.ON_OPPONENT_DISCARDS,
                new ConjureDuplicateOfDiscardedCardIntoChosenPlayerHandEffect());
    }
}
