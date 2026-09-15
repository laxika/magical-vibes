package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.FreshVolunteers;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PowerMatrix.class, FreshVolunteers.class})
class PowerMatrixTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Power Matrix boosts the target and grants three keywords")
    void boostsAndGrantsKeywords() {
        Permanent matrix = addCreatureReady(player1, new PowerMatrix());
        Permanent target = addCreature(player1);

        harness.activateAbility(player1, 0, null, target.getId());
        assertThat(matrix.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(1);
        assertThat(target.getToughnessModifier()).isEqualTo(1);
        assertThat(target.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(target.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
        assertThat(target.hasKeyword(Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Power Matrix effects wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        addCreatureReady(player1, new PowerMatrix());
        Permanent target = addCreature(player1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(target.hasKeyword(Keyword.FLYING)).isFalse();
        assertThat(target.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
        assertThat(target.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Power Matrix can target a creature an opponent controls")
    void canTargetOpponentsCreature() {
        addCreatureReady(player1, new PowerMatrix());
        Permanent target = addCreature(player2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(1);
        assertThat(target.getToughnessModifier()).isEqualTo(1);
        assertThat(target.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(target.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
        assertThat(target.hasKeyword(Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Power Matrix cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        addCreatureReady(player1, new PowerMatrix());
        Permanent target = addCreatureReady(player1, new PowerMatrix());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }

    private Permanent addCreature(Player player) {
        return addCreatureReady(player, new FreshVolunteers());
    }
}
