package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;

@CardRegistration(set = "GTC", collectorNumber = "124")
public class Hindervines extends Card {

    public Hindervines() {
        // Prevent all combat damage that would be dealt this turn by creatures with no +1/+1 counters on them.
        addEffect(EffectSlot.SPELL, PreventDamageEffect.allCombatExcept(
                new PermanentHasCountersPredicate(CounterType.PLUS_ONE_PLUS_ONE)));
    }
}
