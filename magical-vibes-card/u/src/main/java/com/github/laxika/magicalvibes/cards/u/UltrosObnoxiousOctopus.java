package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.condition.SpellManaSpentAtLeast;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FIN", collectorNumber = "83")
@CardRegistration(set = "FIN", collectorNumber = "442")
public class UltrosObnoxiousOctopus extends Card {

    public UltrosObnoxiousOctopus() {
        CardNotPredicate noncreatureSpell = new CardNotPredicate(new CardTypePredicate(CardType.CREATURE));

        target(TargetFilters.creatureAnOpponentControls()).addTrigger(Triggers.youCastSpell()
                        .withFilter(noncreatureSpell),
                TriggerOptions.DEFAULT.withInterveningIf(new SpellManaSpentAtLeast(4)),
                new TapPermanentsEffect(Subjects.target()),
                new PutCountersEffect(CounterType.STUN));

        addTrigger(Triggers.youCastSpell().withFilter(noncreatureSpell),
                TriggerOptions.DEFAULT.withInterveningIf(new SpellManaSpentAtLeast(8)),
                PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE, 8));
    }
}
