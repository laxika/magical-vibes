package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.GrantAdditionalPlusOnePlusOneCountersToTriggeringCreatureSpellEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedControllerSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "YBRO", collectorNumber = "28")
public class PatchplateResolute extends Card {

    public PatchplateResolute() {
        var boon = RegisterDelayedControllerSpellCastTriggerEffect.oneShotUntilConsumed(
                new CardTypePredicate(CardType.CREATURE),
                List.of(new GrantAdditionalPlusOnePlusOneCountersToTriggeringCreatureSpellEffect(
                        new Fixed(1))));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, boon);
        addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD, boon);
        addUnearth("{1}{W}");
    }
}
