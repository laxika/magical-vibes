package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerHandEmpty;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

import java.util.List;

@CardRegistration(set = "DIS", collectorNumber = "111")
public class GobhobblerRats extends Card {

    public GobhobblerRats() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new ControllerHandEmpty(),
                new StaticBoostEffect(1, 0, GrantScope.SELF)));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new ControllerHandEmpty(),
                new GrantActivatedAbilityEffect(
                        new ActivatedAbility(
                                false,
                                "{B}",
                                List.of(new RegenerateEffect()),
                                "{B}: Regenerate this creature."),
                        GrantScope.SELF)));
    }
}
