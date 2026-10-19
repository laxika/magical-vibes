package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;

import java.util.List;

@CardRegistration(set = "ONS", collectorNumber = "56")
public class SunfireBalm extends Card {

    public SunfireBalm() {
        addEffect(EffectSlot.SPELL, PreventDamageEffect.nextToTarget(4));

        // "When you cycle this card, you may prevent the next 1 damage that would be dealt to any
        // target this turn." The target is announced with the cycling ability and rides on the
        // separate self-cycle trigger, which resolves before the cycling draw.
        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{1}{W}",
                List.of(new DrawCardEffect(1)),
                "Cycling {1}{W} ({1}{W}, Discard this card: Draw a card.)",
                null,
                null,
                null,
                null,
                List.of(),
                0,
                1).withCyclingTriggerTargets());
        addEffect(EffectSlot.ON_SELF_CYCLED, new MayEffect(
                PreventDamageEffect.nextToTarget(1),
                "Prevent the next 1 damage that would be dealt to any target?"));
    }
}
