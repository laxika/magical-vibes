package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Max;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;

@CardRegistration(set = "4ED", collectorNumber = "352")
public class TheRack extends Card {

    public TheRack() {
        // "As this artifact enters, choose an opponent. At the beginning of the chosen player's
        // upkeep, this artifact deals X damage to that player, where X is 3 minus the number of
        // cards in their hand." The chosen opponent is implicit (single-opponent model, like Cursed
        // Rack), so OPPONENT_UPKEEP_TRIGGERED fires on that opponent's upkeep with them as target.
        // X = max(0, 3 - cards in hand), floored so a full hand deals no damage.
        addEffect(EffectSlot.OPPONENT_UPKEEP_TRIGGERED,
                new DealDamageToPlayersEffect(
                        new Max(new Fixed(0),
                                new Sum(new Fixed(3), new Scaled(new CardsInHand(CountScope.TARGET_PLAYER), -1))),
                        DamageRecipient.TARGET_PLAYER));
    }
}
