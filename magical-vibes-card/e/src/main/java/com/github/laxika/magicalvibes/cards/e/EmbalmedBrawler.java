package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.AmplifyEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "LGN", collectorNumber = "69")
public class EmbalmedBrawler extends Card {

    public EmbalmedBrawler() {
        addStaticEffect(new AmplifyEffect(
                1, new CardSubtypePredicate(CardSubtype.ZOMBIE)));

        addTrigger(List.of(Triggers.attacks(), Triggers.blocks()),
                new LoseLifeEffect(new CountersOnSource(CounterType.PLUS_ONE_PLUS_ONE), LoseLifeRecipient.CONTROLLER));
    }
}
