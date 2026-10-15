package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MorphicTideEffect;

import java.util.List;

@CardRegistration(set = "MH2", collectorNumber = "129")
public class GlimpseOfTomorrow extends Card {

    public GlimpseOfTomorrow() {
        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{R}{R}",
                List.of(),
                "Suspend 3—{R}{R}",
                ActivationTimingRestriction.SORCERY_SPEED
        ).withSuspendsSourceFromHand(3));

        addEffect(EffectSlot.SPELL, MorphicTideEffect.glimpseOfTomorrow());
    }
}
