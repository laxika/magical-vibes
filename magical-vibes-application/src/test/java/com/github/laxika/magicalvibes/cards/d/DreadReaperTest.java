package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DreadReaper.class, GrizzlyBears.class})
class DreadReaperTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the creature spell puts the ETB trigger on the stack")
    void resolvingPutsEtbOnStack() {
        castDreadReaper();
        harness.passBothPriorities(); // resolve creature spell

        harness.assertOnBattlefield(player1, "Dread Reaper");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
    }

    @Test
    @DisplayName("ETB trigger makes the controller lose 5 life")
    void etbMakesControllerLose5Life() {
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        castDreadReaper();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 5);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Opponent's life is unaffected by the ETB trigger")
    void opponentLifeUnaffected() {
        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        castDreadReaper();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore);
    }

    @Test
    @DisplayName("A creature without flying cannot block Dread Reaper")
    void cannotBeBlockedByNonFlyingCreature() {
        Permanent attacker = addCreatureReady(player1, new DreadReaper());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(
                        gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                        gd.playerBattlefields.get(player1.getId()).indexOf(attacker)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot block Dread Reaper (flying)");
    }

    @Test
    @DisplayName("The trigger still causes life loss after Dread Reaper leaves the battlefield")
    void triggerResolvesWithoutSource() {
        harness.setLife(player1, 20);
        castDreadReaper();
        harness.passBothPriorities();
        harness.assertLife(player1, 20);

        Permanent reaper = findPermanent(player1, "Dread Reaper");
        gd.playerBattlefields.get(player1.getId()).remove(reaper);
        harness.setGraveyard(player1, List.of(reaper.getCard()));

        harness.passBothPriorities();

        harness.assertLife(player1, 15);
        harness.assertNotOnBattlefield(player1, "Dread Reaper");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent casting Dread Reaper loses life instead of the first player")
    void opponentControllerLosesLife() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new DreadReaper(), "{3}{B}{B}{B}");

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Dread Reaper");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 15);
        assertThat(gd.stack).isEmpty();
    }

    private void castDreadReaper() {
        harness.castFromHand(player1, new DreadReaper(), "{3}{B}{B}{B}");
    }
}
