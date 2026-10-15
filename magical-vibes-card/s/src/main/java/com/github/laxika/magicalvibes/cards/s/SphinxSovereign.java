package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.SourceUntapped;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;

@CardRegistration(set = "ALA", collectorNumber = "196")
public class SphinxSovereign extends Card {

    public SphinxSovereign() {
        // At the beginning of your end step, you gain 3 life if this creature is untapped.
        // Otherwise, each opponent loses 3 life. (Flying is auto-loaded from Scryfall.)
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, new ConditionalReplacementEffect(
                new SourceUntapped(),
                new LoseLifeEffect(3, LoseLifeRecipient.EACH_OPPONENT),
                new GainLifeEffect(3)));
    }
}
