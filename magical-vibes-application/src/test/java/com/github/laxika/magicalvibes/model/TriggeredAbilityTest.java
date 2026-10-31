package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.condition.SourceCardInGraveyard;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.trigger.LegacySlotTrigger;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
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
        LegacySlotTrigger trigger = new LegacySlotTrigger(EffectSlot.GRAVEYARD_UPKEEP_TRIGGERED);

        TriggeredAbility ability = new TriggeredAbility(trigger, TriggerOptions.DEFAULT, ZOMBIE);

        assertThat(ability.conditions()).containsExactly(new TriggerCondition(trigger, AbilityZones.GRAVEYARD));
        assertThat(ability.effects()).isEqualTo(ZOMBIE);
    }

    @Test
    void keepsEffectsThatHappenAtTheSameTimeAsTheOneBefore() {
        EffectRegistration first = new EffectRegistration(new DrawCardEffect(1));
        EffectRegistration simultaneous = new EffectRegistration(new DrawCardEffect(2), TriggerMode.NORMAL, true);

        TriggeredAbility ability = new TriggeredAbility(new LegacySlotTrigger(EffectSlot.ON_DEATH),
                TriggerOptions.DEFAULT, List.of(first, simultaneous));

        assertThat(ability.effects()).containsExactly(first, simultaneous);
    }

    @Test
    void holdsUnmodifiableCopiesOfItsConditionsAndEffects() {
        List<TriggerCondition> conditions =
                new ArrayList<>(List.of(new TriggerCondition(new LegacySlotTrigger(EffectSlot.ON_DEATH))));
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
        LegacySlotTrigger trigger = new LegacySlotTrigger(EffectSlot.ON_DEATH);

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

        assertThatThrownBy(() -> new TriggeredAbility(new LegacySlotTrigger(EffectSlot.ON_DEATH),
                TriggerOptions.DEFAULT, List.of(simultaneous)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void aLegacySlotTriggerIsTheOnlyConditionOfItsAbility() {
        List<TriggerCondition> twoSlots = List.of(
                new TriggerCondition(new LegacySlotTrigger(EffectSlot.ON_ENTER_BATTLEFIELD)),
                new TriggerCondition(new LegacySlotTrigger(EffectSlot.ON_ATTACK)));

        assertThatThrownBy(() -> new TriggeredAbility(twoSlots, TriggerOptions.DEFAULT, ZOMBIE))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void aLegacySlotTriggerTakesNoTriggerOptions() {
        LegacySlotTrigger trigger = new LegacySlotTrigger(EffectSlot.GRAVEYARD_UPKEEP_TRIGGERED);

        assertThatThrownBy(() -> new TriggeredAbility(trigger,
                TriggerOptions.DEFAULT.withInterveningIf(new SourceCardInGraveyard()), ZOMBIE))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new TriggeredAbility(trigger,
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN), ZOMBIE))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
