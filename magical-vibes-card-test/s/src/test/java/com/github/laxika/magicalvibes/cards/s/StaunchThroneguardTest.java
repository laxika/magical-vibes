package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StaunchThroneguard.class})
class StaunchThroneguardTest extends BaseCardTest {

    @Test
    @DisplayName("Makes its controller the monarch when it enters")
    void makesControllerMonarchWhenItEnters() {
        castStaunchThroneguard(player1);

        assertThat(gd.monarchPlayerId).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Replaces the existing monarch when it enters")
    void replacesExistingMonarch() {
        gd.monarchPlayerId = player2.getId();

        castStaunchThroneguard(player1);

        assertThat(gd.monarchPlayerId).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Becoming the monarch waits for the enter trigger to resolve")
    void monarchChangesOnlyWhenTriggerResolves() {
        gd.monarchPlayerId = player2.getId();
        harness.castFromHand(player1, new StaunchThroneguard(), "{5}");

        assertThat(gd.monarchPlayerId).isEqualTo(player2.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Staunch Throneguard");
        assertThat(gd.monarchPlayerId).isEqualTo(player2.getId());
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        assertThat(gd.monarchPlayerId).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("An opponent becomes monarch when their Throneguard enters without being cast")
    void noncastEntryMakesItsControllerMonarch() {
        gd.monarchPlayerId = player1.getId();

        harness.enterBattlefieldAndReturn(player2, new StaunchThroneguard());
        resolveAllTriggers();

        assertThat(gd.monarchPlayerId).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Entering while already monarch preserves the designation")
    void enteringWhileAlreadyMonarch() {
        gd.monarchPlayerId = player1.getId();

        castStaunchThroneguard(player1);

        assertThat(gd.monarchPlayerId).isEqualTo(player1.getId());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Attacking with vigilance leaves Throneguard untapped")
    void attackingDoesNotTapThroneguard() {
        Permanent throneguard = addCreatureReady(player1, new StaunchThroneguard());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(throneguard.isTapped()).isFalse();
        assertThat(throneguard.isAttacking()).isTrue();
    }

    private void castStaunchThroneguard(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player, new StaunchThroneguard(), "{5}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
