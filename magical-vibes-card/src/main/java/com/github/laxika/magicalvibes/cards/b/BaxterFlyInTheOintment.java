package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;

@CardRegistration(set = "TMC", collectorNumber = "10")
public class BaxterFlyInTheOintment extends Card {

    public BaxterFlyInTheOintment() {
        // Whenever Baxter enters or attacks, each creature you control with a counter on it gains
        // flying until end of turn.
        GrantKeywordEffect flyingForCounteredCreatures = new GrantKeywordEffect(
                Keyword.FLYING,
                GrantScope.ALL_OWN_CREATURES,
                new PermanentHasCountersPredicate(CounterType.ANY));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, flyingForCounteredCreatures);
        addEffect(EffectSlot.ON_ATTACK, flyingForCounteredCreatures);

        // Whenever you draw a card, put a +1/+1 counter on Baxter.
        addEffect(EffectSlot.ON_CONTROLLER_DRAWS,
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
