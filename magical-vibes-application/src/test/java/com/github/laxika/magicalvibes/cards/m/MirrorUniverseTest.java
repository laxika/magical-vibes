package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MirrorUniverse.class})
class MirrorUniverseTest extends BaseCardTest {

    @Test
    @DisplayName("Exchanges life totals with an opponent and sacrifices itself")
    void exchangesLifeTotalsAndSacrificesItself() {
        addReadyMirror(player1);
        harness.setLife(player1, 5);
        harness.setLife(player2, 20);

        advanceToUpkeep(player1);
        harness.activateAbility(player1, 0, null, player2.getId());

        harness.assertNotOnBattlefield(player1, "Mirror Universe");
        harness.assertInGraveyard(player1, "Mirror Universe");
        harness.assertLife(player1, 5);
        harness.assertLife(player2, 20);

        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 5);
    }

    @Test
    @DisplayName("Cannot target its controller")
    void cannotTargetItsController() {
        addReadyMirror(player1);
        advanceToUpkeep(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
    }

    @Test
    @DisplayName("Can only be activated during its controller's upkeep")
    void onlyActivatesDuringControllersUpkeep() {
        addReadyMirror(player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("during your upkeep");

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof MirrorUniverse);
    }

    @Test
    @DisplayName("Requires the artifact to be untapped")
    void requiresUntappedArtifact() {
        Permanent mirror = addReadyMirror(player1);
        advanceToUpkeep(player1);
        mirror.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Cannot activate during an opponent's upkeep")
    void cannotActivateDuringOpponentsUpkeep() {
        addReadyMirror(player1);
        advanceToUpkeep(player2);
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("during your upkeep");

        harness.assertOnBattlefield(player1, "Mirror Universe");
    }

    @Test
    @DisplayName("Exchanges life totals as they stand when the ability resolves")
    void exchangesLifeTotalsAtResolution() {
        addReadyMirror(player1);
        advanceToUpkeep(player1);
        harness.setLife(player1, 5);
        harness.setLife(player2, 20);
        harness.activateAbility(player1, 0, null, player2.getId());

        harness.setLife(player1, 8);
        harness.setLife(player2, 14);
        harness.passBothPriorities();

        harness.assertLife(player1, 14);
        harness.assertLife(player2, 8);
    }

    @Test
    @DisplayName("Equal life totals remain unchanged and the artifact is still sacrificed")
    void equalLifeTotalsStillPaySacrificeCost() {
        addReadyMirror(player1);
        advanceToUpkeep(player1);
        harness.setLife(player1, 12);
        harness.setLife(player2, 12);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 12);
        harness.assertLife(player2, 12);
        harness.assertNotOnBattlefield(player1, "Mirror Universe");
        harness.assertInGraveyard(player1, "Mirror Universe");
    }

    @Test
    @DisplayName("The opponent can activate their own Mirror Universe during their upkeep")
    void exchangesWithOpposingController() {
        addReadyMirror(player2);
        advanceToUpkeep(player2);
        harness.setLife(player1, 18);
        harness.setLife(player2, 3);

        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 3);
        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player2, "Mirror Universe");
    }

    private Permanent addReadyMirror(com.github.laxika.magicalvibes.model.Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new MirrorUniverse());
        permanent.setSummoningSick(false);
        return permanent;
    }
}
