package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Max;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

/**
 * Stabwhisker the Odious — flipped face of Nezumi Shortfang.
 * 3/3 legendary Rat Shaman.
 * At the beginning of each opponent's upkeep, that player loses 1 life for each card fewer than
 * three in their hand.
 */
public class StabwhiskerTheOdious extends Card {

    public StabwhiskerTheOdious() {
        // At the beginning of each opponent's upkeep, that player loses 1 life for each card fewer
        // than three in their hand. A hand of three or more cards loses no life, hence the Max floor.
        addTrigger(Triggers.upkeep(ActivePlayer.OPPONENT), new LoseLifeEffect(
                new Max(new Fixed(0),
                        new Sum(new Fixed(3), new Scaled(new CardsInHand(CountScope.TARGET_PLAYER), -1))),
                LoseLifeRecipient.TARGET_PLAYER));
    }
}
