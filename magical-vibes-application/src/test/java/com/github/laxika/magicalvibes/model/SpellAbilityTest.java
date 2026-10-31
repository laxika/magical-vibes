package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SpellAbilityTest {

    @Test
    void keepsItsEffectsInOrderWithTheirSimultaneousFlags() {
        // Char: "deals 4 damage to any target and 2 damage to you" is one damage event.
        EffectRegistration toAnyTarget = new EffectRegistration(new DealDamageToAnyTargetEffect(4));
        EffectRegistration toYou = new EffectRegistration(
                new DealDamageToPlayersEffect(2, DamageRecipient.CONTROLLER), TriggerMode.NORMAL, true);

        SpellAbility ability = new SpellAbility(List.of(toAnyTarget, toYou));

        assertThat(ability.effects()).containsExactly(toAnyTarget, toYou);
    }

    @Test
    void holdsAnUnmodifiableCopyOfItsEffects() {
        List<EffectRegistration> source = new ArrayList<>(List.of(new EffectRegistration(new DrawCardEffect(1))));
        SpellAbility ability = new SpellAbility(source);

        source.add(new EffectRegistration(new DrawCardEffect(2)));

        assertThat(ability.effects()).hasSize(1);
        assertThatThrownBy(() -> ability.effects().add(new EffectRegistration(new DrawCardEffect(3))))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void needsAtLeastOneEffect() {
        assertThatThrownBy(() -> new SpellAbility(List.of())).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void firstEffectCannotBeSimultaneousWithAPreviousOne() {
        EffectRegistration simultaneous = new EffectRegistration(new DrawCardEffect(1), TriggerMode.NORMAL, true);

        assertThatThrownBy(() -> new SpellAbility(List.of(simultaneous)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void aSpellAbilityDoesNotTrigger() {
        EffectRegistration independent = new EffectRegistration(new DrawCardEffect(1), TriggerMode.INDEPENDENT);

        assertThatThrownBy(() -> new SpellAbility(List.of(independent)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("INDEPENDENT");
    }
}
