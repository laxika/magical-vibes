package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.condition.SourceIsTapped;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;

@CardRegistration(set = "SLD", collectorNumber = "2252")
@CardRegistration(set = "SPG", collectorNumber = "13")
@CardRegistration(set = "C20", collectorNumber = "9")
public class KalamaxTheStormsire extends Card {

    public KalamaxTheStormsire() {
        CardTypePredicate instant = new CardTypePredicate(CardType.INSTANT);
        addTrigger(Triggers.youCastSpell().withFilter(instant).nthThisTurn(1),
                TriggerOptions.DEFAULT.withInterveningIf(new SourceIsTapped()),
                new CopyTriggeringSpellEffect());
        addTrigger(Triggers.youCopySpell().withFilter(instant),
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
