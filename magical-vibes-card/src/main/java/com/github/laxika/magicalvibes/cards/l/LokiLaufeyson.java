package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedControllerSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryManaValueAtMostSourcePowerPredicate;

import java.util.List;

@CardRegistration(set = "MSH", collectorNumber = "143")
public class LokiLaufeyson extends Card {

    public LokiLaufeyson() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new RegisterDelayedControllerSpellCastTriggerEffect(
                        new CardAnyOfPredicate(List.of(
                                new CardTypePredicate(CardType.INSTANT),
                                new CardTypePredicate(CardType.SORCERY))),
                        new StackEntryManaValueAtMostSourcePowerPredicate(),
                        List.of(new CopyTriggeringSpellEffect()),
                        true,
                        false,
                        null)),
                "{1}, {T}: When you next cast an instant or sorcery spell with mana value less than or equal to Loki's power this turn, copy that spell. You may choose new targets for the copy."
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}{R}",
                List.of(new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, 2)),
                "Power-up — {4}{R}: Put two +1/+1 counters on Loki. Activate each power-up ability only once. Reduce the cost by his mana cost if he entered this turn."
        ).withPowerUp());
    }
}
