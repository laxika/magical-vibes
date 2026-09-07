package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfThenEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "WOE", collectorNumber = "148")
public class RotisserieElemental extends Card {

    public RotisserieElemental() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, SequenceEffect.of(
                new PutCountersOnSelfEffect(CounterType.SKEWER),
                new MayEffect(
                        new SacrificeSelfThenEffect(new ExileTopCardMayPlayThisTurnEffect(
                                new CountersOnSource(CounterType.SKEWER), false)),
                        "You may sacrifice Rotisserie Elemental.")));
    }
}
