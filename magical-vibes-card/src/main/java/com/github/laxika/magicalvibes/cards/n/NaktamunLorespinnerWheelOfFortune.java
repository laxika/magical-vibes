package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.w.WheelOfFortune;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AnyPlayerHandAtMost;
import com.github.laxika.magicalvibes.model.effect.BecomePreparedEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;

/** Naktamun Lorespinner // Wheel of Fortune (SOC 33). */
@CardRegistration(set = "SOC", collectorNumber = "33")
@CardRegistration(set = "SOC", collectorNumber = "81")
public class NaktamunLorespinnerWheelOfFortune extends Card {

    public NaktamunLorespinnerWheelOfFortune() {
        setBackFaceCard(new WheelOfFortune());

        // At the beginning of your upkeep, if a player has one or fewer cards in hand, this creature becomes prepared.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new ConditionalEffect(
                new AnyPlayerHandAtMost(1), new BecomePreparedEffect()));
    }

    @Override
    public String getBackFaceClassName() {
        return "WheelOfFortune";
    }
}
