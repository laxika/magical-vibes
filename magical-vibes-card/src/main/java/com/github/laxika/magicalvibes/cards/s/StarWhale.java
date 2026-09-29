package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;

import java.util.List;

@CardRegistration(set = "WHO", collectorNumber = "55")
public class StarWhale extends Card {

    public StarWhale() {
        addEffect(EffectSlot.STATIC, new GrantTriggeredAbilityEffect(
                EffectSlot.ON_BECOMES_TARGET_OF_OPPONENT_SPELL,
                new CounterUnlessPaysEffect(2),
                GrantScope.OWN_CREATURES));

        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{1}{U}",
                List.of(),
                "Suspend 6—{1}{U}",
                ActivationTimingRestriction.SORCERY_SPEED
        ).withSuspendsSourceFromHand(6));
    }
}
