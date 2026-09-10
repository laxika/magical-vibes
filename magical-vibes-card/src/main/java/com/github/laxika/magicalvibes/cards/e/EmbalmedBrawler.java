package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.amount.MatchingCardsInHand;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

@CardRegistration(set = "LGN", collectorNumber = "69")
public class EmbalmedBrawler extends Card {

    public EmbalmedBrawler() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new EnterWithCountersEffect(
                CounterType.PLUS_ONE_PLUS_ONE,
                new MatchingCardsInHand(CountScope.CONTROLLER,
                        new CardSubtypePredicate(CardSubtype.ZOMBIE))));

        addEffect(EffectSlot.ON_ATTACK,
                new LoseLifeEffect(new CountersOnSource(CounterType.PLUS_ONE_PLUS_ONE),
                        LoseLifeRecipient.CONTROLLER));
        addEffect(EffectSlot.ON_BLOCK,
                new LoseLifeEffect(new CountersOnSource(CounterType.PLUS_ONE_PLUS_ONE),
                        LoseLifeRecipient.CONTROLLER));
    }
}
