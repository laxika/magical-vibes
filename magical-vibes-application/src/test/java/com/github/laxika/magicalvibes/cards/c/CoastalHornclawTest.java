package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CoastalHornclaw.class, Island.class})
class CoastalHornclawTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a land grants flying until end of turn")
    void sacrificeLandGrantsFlying() {
        Permanent hornclaw = addCreatureReady(player1, new CoastalHornclaw());
        harness.addToBattlefield(player1, new Island());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, hornclaw, Keyword.FLYING)).isTrue();
        // The land was sacrificed.
        assertThat(countPermanents(player1, "Island")).isZero();
    }

    @Test
    @DisplayName("With multiple lands, prompts which to sacrifice")
    void multipleLandsPromptsChoice() {
        Permanent hornclaw = addCreatureReady(player1, new CoastalHornclaw());
        Permanent landA = harness.addToBattlefieldAndReturn(player1, new Island());
        harness.addToBattlefieldAndReturn(player1, new Island());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, landA.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, hornclaw, Keyword.FLYING)).isTrue();
        assertThat(countPermanents(player1, "Island")).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate without a land to sacrifice")
    void cannotActivateWithoutLand() {
        addCreatureReady(player1, new CoastalHornclaw());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Sacrifice a land");
    }

    @Test
    @DisplayName("Cannot use a nonland permanent to pay the land sacrifice cost")
    void cannotActivateWithOnlyNonLandPermanents() {
        addCreatureReady(player1, new CoastalHornclaw());
        addCreatureReady(player1, new CoastalHornclaw());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Sacrifice a land");
    }

    @Test
    @DisplayName("Cannot sacrifice an opponent's land")
    void cannotSacrificeOpponentsLand() {
        addCreatureReady(player1, new CoastalHornclaw());
        harness.addToBattlefield(player2, new Island());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Sacrifice a land");
        harness.assertOnBattlefield(player2, "Island");
    }

    @Test
    @DisplayName("Flying wears off at end of turn")
    void flyingWearsOffAtEndOfTurn() {
        Permanent hornclaw = addCreatureReady(player1, new CoastalHornclaw());
        harness.addToBattlefield(player1, new Island());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, hornclaw, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, hornclaw, Keyword.FLYING)).isFalse();
    }

}
