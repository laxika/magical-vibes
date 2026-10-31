package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StaticAbilityTest {

    private static final CardEffect ANTHEM = new StaticBoostEffect(1, 1, GrantScope.OWN_CREATURES);

    @Test
    void aPermanentsStaticAbilityFunctionsOnTheBattlefield() {
        assertThat(new StaticAbility(List.of(ANTHEM)).zones()).isEqualTo(AbilityZones.BATTLEFIELD);
    }

    @Test
    void keepsTheZonesItIsGiven() {
        assertThat(new StaticAbility(List.of(ANTHEM), AbilityZones.COMMAND).zones())
                .isEqualTo(AbilityZones.COMMAND);
    }

    @Test
    void holdsAnUnmodifiableCopyOfItsEffects() {
        List<CardEffect> source = new ArrayList<>(List.of(ANTHEM));
        StaticAbility ability = new StaticAbility(source);

        source.add(new StaticBoostEffect(2, 2, GrantScope.OWN_CREATURES));

        assertThat(ability.effects()).containsExactly(ANTHEM);
        assertThatThrownBy(() -> ability.effects().add(ANTHEM)).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void needsAtLeastOneEffectAndItsZones() {
        assertThatThrownBy(() -> new StaticAbility(List.of())).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new StaticAbility(List.of(ANTHEM), null)).isInstanceOf(NullPointerException.class);
    }
}
