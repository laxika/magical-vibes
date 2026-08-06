package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GruulCharmTest extends BaseCardTest {

    private void castCharm(int modeIndex) {
        harness.setHand(player1, List.of(new GruulCharm()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, modeIndex, null);
        harness.passBothPriorities();
    }

    @Nested
    @DisplayName("Mode 0: Creatures without flying can't block this turn")
    class CantBlockMode {

        @Test
        @DisplayName("A ground creature can no longer block")
        void groundCreatureCannotBlock() {
            Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
            attacker.setSummoningSick(false);
            Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
            blocker.setSummoningSick(false);

            castCharm(0);

            attacker.setAttacking(true);
            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.DECLARE_BLOCKERS);
            harness.clearPriorityPassed();
            harness.beginBlockerDeclarationInput();

            assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("A flying creature can still block")
        void flyingCreatureCanStillBlock() {
            Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
            attacker.setSummoningSick(false);
            Permanent blocker = harness.addToBattlefieldAndReturn(player2, new AirElemental());
            blocker.setSummoningSick(false);

            castCharm(0);

            attacker.setAttacking(true);
            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.DECLARE_BLOCKERS);
            harness.clearPriorityPassed();
            harness.beginBlockerDeclarationInput();

            gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

            assertThat(blocker.isBlocking()).isTrue();
        }
    }

    @Nested
    @DisplayName("Mode 1: Gain control of all permanents you own")
    class ReclaimMode {

        @Test
        @DisplayName("Reclaims a creature an opponent stole")
        void reclaimsStolenCreature() {
            // Player 2 controls a creature player 1 owns — the engine's representation of a steal
            // is battlefield membership plus an ownership entry in stolenCreatures.
            Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
            gd.stolenCreatures.put(bears.getId(), player1.getId());

            castCharm(1);

            assertThat(gd.playerBattlefields.get(player1.getId())).contains(bears);
            assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bears);
        }

        @Test
        @DisplayName("Leaves permanents owned by the opponent alone")
        void leavesOpponentOwnedPermanentsAlone() {
            Permanent theirs = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

            castCharm(1);

            assertThat(gd.playerBattlefields.get(player2.getId())).contains(theirs);
            assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(theirs);
        }
    }

    @Nested
    @DisplayName("Mode 2: 3 damage to each creature with flying")
    class FlyingSweepMode {

        @Test
        @DisplayName("Kills flyers on both sides and spares ground creatures")
        void damagesOnlyFlyers() {
            harness.addToBattlefield(player1, new SuntailHawk());
            harness.addToBattlefield(player2, new SuntailHawk());
            harness.addToBattlefield(player2, new GrizzlyBears());
            harness.addToBattlefield(player2, new AirElemental());

            castCharm(2);

            harness.assertNotOnBattlefield(player1, "Suntail Hawk");
            harness.assertNotOnBattlefield(player2, "Suntail Hawk");
            harness.assertOnBattlefield(player2, "Grizzly Bears");
            harness.assertOnBattlefield(player2, "Air Elemental");
        }

        @Test
        @DisplayName("Deals no damage to players")
        void dealsNoDamageToPlayers() {
            int startingLife = gd.playerLifeTotals.get(player2.getId());

            harness.addToBattlefield(player2, new SuntailHawk());

            castCharm(2);

            harness.assertLife(player2, startingLife);
        }
    }
}
