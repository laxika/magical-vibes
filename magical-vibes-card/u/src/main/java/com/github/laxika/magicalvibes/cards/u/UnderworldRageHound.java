package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ExileNCardsFromGraveyardCastingCost;
import com.github.laxika.magicalvibes.model.GraveyardCast;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.SourceWasCastWithEscape;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.MustAttackEffect;

import java.util.List;

@CardRegistration(set = "THB", collectorNumber = "163")
public class UnderworldRageHound extends Card {

    public UnderworldRageHound() {
        addEffect(EffectSlot.STATIC, new MustAttackEffect());

        addCastingOption(new GraveyardCast(null, "{3}{R}", List.of(
                new ExileNCardsFromGraveyardCastingCost(null, "other cards", 3)), null, false, false, true));

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(
                new SourceWasCastWithEscape(),
                new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new Fixed(1))));
    }
}
