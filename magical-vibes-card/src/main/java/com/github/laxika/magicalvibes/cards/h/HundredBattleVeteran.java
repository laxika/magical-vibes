package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardCast;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.CastFromZone;
import com.github.laxika.magicalvibes.model.condition.ControlledCreatureCounterKindsAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "TDM", collectorNumber = "82")
public class HundredBattleVeteran extends Card {

    public HundredBattleVeteran() {
        addCastingOption(new GraveyardCast());
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new ControlledCreatureCounterKindsAtLeast(3),
                new StaticBoostEffect(2, 4, GrantScope.SELF)));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(
                new CastFromZone(Zone.GRAVEYARD),
                new EnterWithCountersEffect(CounterType.FINALITY, new Fixed(1))));
    }
}
