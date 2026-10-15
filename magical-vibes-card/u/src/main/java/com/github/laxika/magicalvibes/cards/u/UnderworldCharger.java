package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ExileNCardsFromGraveyardCastingCost;
import com.github.laxika.magicalvibes.model.GraveyardCast;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.CastFromZone;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;

import java.util.List;

@CardRegistration(set = "THB", collectorNumber = "120")
public class UnderworldCharger extends Card {

    public UnderworldCharger() {
        addEffect(EffectSlot.STATIC, new CantBlockEffect());
        addCastingOption(new GraveyardCast(null, "{4}{B}", List.of(
                new ExileNCardsFromGraveyardCastingCost(null, "other cards", 3))));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(new CastFromZone(Zone.GRAVEYARD),
                new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new Fixed(2))));
    }
}
