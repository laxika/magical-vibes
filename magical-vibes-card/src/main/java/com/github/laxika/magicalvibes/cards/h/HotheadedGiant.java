package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.ControllerCastAnotherSpellThisTurn;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;

@CardRegistration(set = "EVE", collectorNumber = "57")
public class HotheadedGiant extends Card {

    public HotheadedGiant() {
        // Enters with two -1/-1 counters unless you've cast another red spell this turn. (Haste is auto-loaded.)
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(
                new NotCondition(new ControllerCastAnotherSpellThisTurn(new CardColorPredicate(CardColor.RED))),
                new EnterWithCountersEffect(CounterType.MINUS_ONE_MINUS_ONE, new Fixed(2))));
    }
}
