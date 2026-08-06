package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.CardsInHandAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetOpponentOrPlaneswalkerEffect;

/**
 * Thumbscrews — "At the beginning of your upkeep, if you have five or more cards in hand,
 * this artifact deals 1 damage to target opponent or planeswalker."
 *
 * <p>The hand-size clause is an intervening "if" (CR 603.4): it is checked as the upkeep begins —
 * the ability doesn't trigger (and no target is chosen) with four or fewer cards in hand — and again
 * on resolution. The large-hand mirror of Scalding Tongs.</p>
 */
@CardRegistration(set = "TMP", collectorNumber = "312")
public class Thumbscrews extends Card {

    public Thumbscrews() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new ConditionalEffect(
                new CardsInHandAtLeast(5),
                new DealDamageToTargetOpponentOrPlaneswalkerEffect(1)));
    }
}
