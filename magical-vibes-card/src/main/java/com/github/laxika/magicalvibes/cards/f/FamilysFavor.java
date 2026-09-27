package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToTargetUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceThenEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "NCC", collectorNumber = "59")
@CardRegistration(set = "NCC", collectorNumber = "159")
public class FamilysFavor extends Card {

    public FamilysFavor() {
        target(TargetFilters.attackingCreature())
                .addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK,
                        new PutCounterOnTargetPermanentEffect(CounterType.SHIELD))
                .addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK,
                        new GrantEffectToTargetUntilEndOfTurnEffect(
                                EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                                new RemoveCounterFromSourceThenEffect(
                                        CounterType.SHIELD, new DrawCardEffect())));
    }
}
