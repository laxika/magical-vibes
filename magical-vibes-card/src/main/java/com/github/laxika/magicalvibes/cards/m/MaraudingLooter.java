package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawAndDiscardCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.RaidConditionalEffect;

@CardRegistration(set = "XLN", collectorNumber = "225")
public class MaraudingLooter extends Card {

    public MaraudingLooter() {
        // Raid — At the beginning of your end step, if you attacked this turn,
        // you may draw a card. If you do, discard a card.
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, new RaidConditionalEffect(
                new MayEffect(new DrawAndDiscardCardEffect(), "Draw a card and discard a card?")
        ));
    }
}
