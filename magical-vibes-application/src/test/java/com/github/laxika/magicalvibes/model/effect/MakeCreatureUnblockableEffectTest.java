package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MakeCreatureUnblockableEffectTest {

    @Test
    @DisplayName("A set of permanents is refused: that rule covers later creatures too (CR 611.2c)")
    void refusesASet() {
        assertThatThrownBy(() -> MakeCreatureUnblockableEffect.of(
                Subjects.controlledBy(Subjects.you(), new PermanentIsCreaturePredicate())))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("A target restriction is refused for a subject that doesn't target")
    void refusesARestrictionWithoutATarget() {
        assertThatThrownBy(() -> new MakeCreatureUnblockableEffect(Subjects.source(), Duration.UNTIL_END_OF_TURN,
                new PermanentIsCreaturePredicate(), false, false))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
