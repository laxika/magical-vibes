package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SylvanSafekeeper.class, Forest.class, GrizzlyBears.class})
class SylvanSafekeeperTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a land grants shroud to a creature you control")
    void sacrificeLandGrantsShroudToControlledCreature() {
        harness.addToBattlefield(player1, new SylvanSafekeeper());
        harness.addToBattlefield(player1, new Forest());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Forest");
        assertThat(bears.hasKeyword(Keyword.SHROUD)).isTrue();
    }

    @Test
    @DisplayName("The granted shroud wears off at end of turn")
    void shroudWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new SylvanSafekeeper());
        harness.addToBattlefield(player1, new Forest());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();
        assertThat(bears.hasKeyword(Keyword.SHROUD)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.hasKeyword(Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("An opponent's creature cannot be targeted")
    void onlyControlledCreatureCanBeTargeted() {
        harness.addToBattlefield(player1, new SylvanSafekeeper());
        harness.addToBattlefield(player1, new Forest());
        Permanent enemyBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, enemyBears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    @Test
    @DisplayName("The ability cannot be activated without a land to sacrifice")
    void requiresLandToSacrifice() {
        harness.addToBattlefield(player1, new SylvanSafekeeper());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
