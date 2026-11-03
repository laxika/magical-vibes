package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.condition.SpellManaSpentGreaterThanSourcePower;
import com.github.laxika.magicalvibes.model.effect.GrantFlashToCardTypeEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsColorlessPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "BRO", collectorNumber = "237")
@CardRegistration(set = "SLD", collectorNumber = "2286")
public class LiberatorUrzasBattlethopter extends Card {

    public LiberatorUrzasBattlethopter() {
        addStaticEffect(new GrantFlashToCardTypeEffect(new CardAnyOfPredicate(List.of(
                new CardIsColorlessPredicate(),
                new CardTypePredicate(CardType.ARTIFACT)
        ))));
        addTrigger(Triggers.youCastSpell().anySpell(),
                TriggerOptions.DEFAULT.withInterveningIf(new SpellManaSpentGreaterThanSourcePower()),
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
