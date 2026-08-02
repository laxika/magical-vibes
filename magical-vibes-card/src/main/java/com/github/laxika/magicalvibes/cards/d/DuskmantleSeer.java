package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachPlayerRevealsTopCardLosesLifeEqualToManaValueThenToHandEffect;

@CardRegistration(set = "GTC", collectorNumber = "159")
public class DuskmantleSeer extends Card {

    public DuskmantleSeer() {
        // Flying is auto-loaded from Scryfall.
        // At the beginning of your upkeep, each player reveals the top card of their library,
        // loses life equal to that card's mana value, then puts it into their hand.
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new EachPlayerRevealsTopCardLosesLifeEqualToManaValueThenToHandEffect());
    }
}
