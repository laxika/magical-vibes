package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;

import java.util.List;

@CardRegistration(set = "HOU", collectorNumber = "44")
public class SeerOfTheLastTomorrow extends Card {

    public SeerOfTheLastTomorrow() {
        // {U}, {T}, Discard a card: Target player mills three cards.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{U}",
                List.of(new DiscardCardTypeCost(null, null), new MillEffect(3, MillRecipient.TARGET_PLAYER)),
                "{U}, {T}, Discard a card: Target player mills three cards."
        ));
    }
}
