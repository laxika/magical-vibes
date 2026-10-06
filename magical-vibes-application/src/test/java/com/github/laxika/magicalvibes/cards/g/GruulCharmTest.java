package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.Flight;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GruulCharm.class, GrizzlyBears.class, AirElemental.class, SuntailHawk.class, Flight.class, Mountain.class})
class GruulCharmTest extends BaseCardTest {

    private void castCharm(int modeIndex) {
        harness.setHand(player1, List.of(new GruulCharm()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, modeIndex, null);
        harness.passBothPriorities();
    }

    @Nested
    @CardUsed({GruulCharm.class, GrizzlyBears.class, AirElemental.class, Flight.class})
    @DisplayName("Mode 0: Creatures without flying can't block this turn")
    class CantBlockMode {

        @Test
        @DisplayName("A ground creature can no longer block")
        void groundCreatureCannotBlock() {
            Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
            addCreatureReady(player2, new GrizzlyBears());

            castCharm(0);

            attacker.setAttacking(true);
            prepareDeclareBlockers();

            assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("A flying creature can still block")
        void flyingCreatureCanStillBlock() {
            Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
            Permanent blocker = addCreatureReady(player2, new AirElemental());

            castCharm(0);

            attacker.setAttacking(true);
            prepareDeclareBlockers();

            gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

            assertThat(blocker.isBlocking()).isTrue();
        }

        @Test
        void groundCreatureEnteringAfterResolutionCannotBlock() {
            Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
            castCharm(0);
            harness.enterBattlefieldAndReturn(player2, new GrizzlyBears());

            attacker.setAttacking(true);
            prepareDeclareBlockers();

            assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        void creatureGainingFlyingAfterResolutionCanBlock() {
            Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
            Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
            castCharm(0);

            harness.setHand(player2, List.of(new Flight()));
            harness.addMana(player2, ManaColor.BLUE, 1);
            harness.forceActivePlayer(player2);
            harness.castEnchantment(player2, 0, blocker.getId());
            harness.passBothPriorities();

            attacker.setAttacking(true);
            prepareDeclareBlockers();
            gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

            assertThat(blocker.isBlocking()).isTrue();
        }
    }

    @Nested
    @CardUsed({GruulCharm.class, GrizzlyBears.class, Mountain.class})
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

        @Test
        void reclaimsOwnedNoncreatureWithoutUntappingIt() {
            Permanent land = harness.addToBattlefieldAndReturn(player2, new Mountain());
            gd.stolenCreatures.put(land.getId(), player1.getId());
            land.tap();

            castCharm(1);

            assertThat(gd.playerBattlefields.get(player1.getId())).contains(land);
            assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(land);
            assertThat(land.isTapped()).isTrue();
        }
    }

    @Nested
    @CardUsed({GruulCharm.class, SuntailHawk.class, GrizzlyBears.class, AirElemental.class})
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

        @Test
        void dealsExactlyThreeDamageToSurvivingFlyersOnBothSides() {
            Permanent ownFlyer = harness.addToBattlefieldAndReturn(player1, new AirElemental());
            Permanent opposingFlyer = harness.addToBattlefieldAndReturn(player2, new AirElemental());
            Permanent groundCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

            castCharm(2);

            assertThat(ownFlyer.getMarkedDamage()).isEqualTo(3);
            assertThat(opposingFlyer.getMarkedDamage()).isEqualTo(3);
            assertThat(groundCreature.getMarkedDamage()).isZero();
            harness.assertLife(player1, 20);
            harness.assertLife(player2, 20);
        }
    }
}
