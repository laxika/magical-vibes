package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.CardsInHandAtMost;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetOpponentOrPlaneswalkerEffect;

/**
 * Scalding Tongs — "At the beginning of your upkeep, if you have three or fewer cards in hand,
 * this artifact deals 1 damage to target opponent or planeswalker."
 *
 * <p>The hand-size clause is an intervening "if" (CR 603.4): it is checked as the upkeep begins —
 * the ability doesn't trigger (and no target is chosen) with four or more cards in hand — and again
 * on resolution.</p>
 */
@CardRegistration(set = "TMP", collectorNumber = "307")
public class ScaldingTongs extends Card {

    public ScaldingTongs() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new ConditionalEffect(
                new CardsInHandAtMost(3),
                new DealDamageToTargetOpponentOrPlaneswalkerEffect(1)));
    }
}
