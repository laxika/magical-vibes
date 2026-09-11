package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DistributeCountersAmongCreaturesOnDeathEffect;
import com.github.laxika.magicalvibes.model.effect.ExileAllCreaturesAndCreateTokensEqualToTotalPowerEffect;
import com.github.laxika.magicalvibes.model.effect.PlaneswalkEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "OPC2", collectorNumber = "17")
public class GrandOssuary extends Card {

    public GrandOssuary() {
        addEffect(EffectSlot.ON_ANY_CREATURE_DIES,
                DistributeCountersAmongCreaturesOnDeathEffect
                        .fromDyingSourcePowerAmongControlledCreatures(CounterType.PLUS_ONE_PLUS_ONE));
        addEffect(EffectSlot.CHAOS_TRIGGERED, SequenceEffect.of(
                new ExileAllCreaturesAndCreateTokensEqualToTotalPowerEffect(
                        new CreateTokenEffect("Saproling", 1, 1, CardColor.GREEN,
                                List.of(CardSubtype.SAPROLING), Set.of(), Set.of())),
                new PlaneswalkEffect()));
    }
}
