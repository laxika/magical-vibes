package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PhyrexianColossusTest extends BaseCardTest {

    // ===== Doesn't untap during untap step =====

    @Test
    @DisplayName("Tapped Phyrexian Colossus does not untap during its controller's untap step")
    void doesNotUntapDuringUntapStep() {
        Permanent colossus = addColossusReady(player1);
        colossus.tap();

        advanceToNextTurn(player2);

        assertThat(colossus.isTapped()).isTrue();
    }

    // ===== Activated ability: pay 8 life to untap =====

    @Test
    @DisplayName("Paying 8 life untaps Phyrexian Colossus")
    void payLifeUntaps() {
        Permanent colossus = addColossusReady(player1);
        colossus.tap();
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(12);
        assertThat(colossus.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate untap ability with fewer than 8 life")
    void cannotActivateWithInsufficientLife() {
        addColossusReady(player1);
        harness.setLife(player1, 7);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough life");
    }

    // ===== Blocking restriction: can't be blocked by fewer than three =====

    @Test
    @DisplayName("Cannot be blocked by fewer than three creatures")
    void cannotBeBlockedByFewerThanThree() {
        Permanent colossus = addColossusReady(player1);
        colossus.setAttacking(true);

        for (int i = 0; i < 3; i++) {
            Permanent blocker = new Permanent(new GrizzlyBears());
            blocker.setSummoningSick(false);
            gd.playerBattlefields.get(player2.getId()).add(blocker);
        }

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("3 or more creatures");
    }

    @Test
    @DisplayName("Can be blocked by three creatures")
    void canBeBlockedByThree() {
        Permanent colossus = addColossusReady(player1);
        colossus.setAttacking(true);

        for (int i = 0; i < 3; i++) {
            Permanent blocker = new Permanent(new GrizzlyBears());
            blocker.setSummoningSick(false);
            gd.playerBattlefields.get(player2.getId()).add(blocker);
        }

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0),
                new BlockerAssignment(2, 0)));

        assertThat(gd.playerBattlefields.get(player2.getId()).get(0).isBlocking()).isTrue();
    }

    // ===== Helpers =====

    private Permanent addColossusReady(Player player) {
        Permanent perm = new Permanent(new PhyrexianColossus());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
