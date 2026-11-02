package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.trigger.Trigger;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TriggeredAbilityTest {

    private static final List<EffectRegistration> ZOMBIE =
            List.of(new EffectRegistration(CreateTokenEffect.blackZombie(1)));

    @Test
    void aSingleTriggerFunctionsFromItsDefaultZones() {
        Trigger trigger = Triggers.chaosEnsues();

        TriggeredAbility ability = new TriggeredAbility(trigger, TriggerOptions.DEFAULT, ZOMBIE);

        assertThat(ability.conditions()).containsExactly(new TriggerCondition(trigger, AbilityZones.COMMAND));
        assertThat(ability.effects()).isEqualTo(ZOMBIE);
    }

    @Test
    void keepsEffectsThatHappenAtTheSameTimeAsTheOneBefore() {
        EffectRegistration first = new EffectRegistration(new DrawCardEffect(1));
        EffectRegistration simultaneous = new EffectRegistration(new DrawCardEffect(2), TriggerMode.NORMAL, true);

        TriggeredAbility ability = new TriggeredAbility(Triggers.youLoseControlOfThis(),
                TriggerOptions.DEFAULT, List.of(first, simultaneous));

        assertThat(ability.effects()).containsExactly(first, simultaneous);
    }

    @Test
    void holdsUnmodifiableCopiesOfItsConditionsAndEffects() {
        List<TriggerCondition> conditions =
                new ArrayList<>(List.of(new TriggerCondition(Triggers.youLoseControlOfThis())));
        List<EffectRegistration> effects = new ArrayList<>(ZOMBIE);
        TriggeredAbility ability = new TriggeredAbility(conditions, TriggerOptions.DEFAULT, effects);

        conditions.clear();
        effects.clear();

        assertThat(ability.conditions()).hasSize(1);
        assertThat(ability.effects()).isEqualTo(ZOMBIE);
        assertThatThrownBy(() -> ability.effects().clear()).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void needsATriggerConditionAndAnEffect() {
        Trigger trigger = Triggers.youLoseControlOfThis();

        assertThatThrownBy(() -> new TriggeredAbility(List.of(), TriggerOptions.DEFAULT, ZOMBIE))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new TriggeredAbility(trigger, TriggerOptions.DEFAULT, List.of()))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new TriggeredAbility(trigger, null, ZOMBIE))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void firstEffectCannotBeSimultaneousWithAPreviousOne() {
        EffectRegistration simultaneous = new EffectRegistration(new DrawCardEffect(1), TriggerMode.NORMAL, true);

        assertThatThrownBy(() -> new TriggeredAbility(Triggers.youLoseControlOfThis(),
                TriggerOptions.DEFAULT, List.of(simultaneous)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
