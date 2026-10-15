package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerWasMonarchAtTurnStart;
import com.github.laxika.magicalvibes.model.effect.BecomeMonarchEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "MOC", collectorNumber = "332")
public class KnightsOfTheBlackRose extends Card {

    public KnightsOfTheBlackRose() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new BecomeMonarchEffect());
        addEffect(EffectSlot.ON_OPPONENT_BECOMES_MONARCH,
                new ConditionalEffect(
                        new ControllerWasMonarchAtTurnStart(),
                        SequenceEffect.of(
                                new LoseLifeEffect(2, LoseLifeRecipient.TRIGGERING_PLAYER),
                                new GainLifeEffect(2))));
    }
}
