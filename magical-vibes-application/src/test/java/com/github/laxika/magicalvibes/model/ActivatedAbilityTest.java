package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ActivatedAbilityTest {

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 101})
    void exactXTargetCountSurvivesAbilityCopies(int x) {
        ActivatedAbility ability = new ActivatedAbility(true, "{X}{U}",
                List.of(ReturnToHandEffect.target()), "Return X target creatures to their owners' hands.",
                TargetFilters.creature()).withExactXTargets();

        for (ActivatedAbility copy : List.of(ability, ability.withGrantSource(UUID.randomUUID()),
                ability.withMaxActivationsPerTurn(1))) {
            assertThat(copy.isXScaledTargets()).isTrue();
            assertThat(copy.getEffectiveMinTargets(x)).isEqualTo(x);
            assertThat(copy.getEffectiveMaxTargets(x)).isEqualTo(x);
        }
    }
}
