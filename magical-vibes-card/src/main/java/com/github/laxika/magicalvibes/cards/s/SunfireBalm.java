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

        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{1}{W}",
                List.of(
                        new MayEffect(
                                PreventDamageEffect.nextToTarget(1),
                                "Prevent the next 1 damage that would be dealt to any target?"),
                        new DrawCardEffect(1)),
                "Cycling {1}{W} ({1}{W}, Discard this card: Draw a card.)",
                List.of(),
                0,
                1));
    }
}
