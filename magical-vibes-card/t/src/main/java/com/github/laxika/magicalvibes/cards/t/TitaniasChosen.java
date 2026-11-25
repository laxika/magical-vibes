package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "USG", collectorNumber = "277")
@CardRegistration(set = "C14", collectorNumber = "221")
@CardRegistration(set = "CMA", collectorNumber = "158")
public class TitaniasChosen extends Card {

    public TitaniasChosen() {
        addTrigger(Triggers.playerCastsSpell().withFilter(new CardColorPredicate(CardColor.GREEN)),
                PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
