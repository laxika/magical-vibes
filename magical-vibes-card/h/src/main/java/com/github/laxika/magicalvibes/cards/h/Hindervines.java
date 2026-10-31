package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;

@CardRegistration(set = "GTC", collectorNumber = "124")
@CardRegistration(set = "MOC", collectorNumber = "301")
public class Hindervines extends Card {

    public Hindervines() {
        // Prevent all combat damage that would be dealt this turn by creatures with no +1/+1 counters on them.
        addSpellEffect(PreventDamageEffect.allCombatExcept(
                new PermanentHasCountersPredicate(CounterType.PLUS_ONE_PLUS_ONE)));
    }
}
