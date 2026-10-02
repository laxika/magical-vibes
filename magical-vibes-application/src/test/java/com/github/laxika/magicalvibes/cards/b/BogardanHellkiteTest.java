package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BogardanHellkite.class, BenalishCavalry.class})
class BogardanHellkiteTest extends BaseCardTest {

    @Test
    @DisplayName("Bogardan Hellkite has flying")
    void hasFlying() {
        Permanent hellkite = addCreatureReady(player1, new BogardanHellkite());

        assertThat(gqs.hasKeyword(gd, hellkite, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Bogardan Hellkite can be cast during an opponent's turn because it has flash")
    void canCastDuringOpponentsTurnWithFlash() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passPriority(player2);

        castBogardanHellkite();

        assertThat(gd.stack).hasSize(1);
    }

    // ===== ETB trigger: deal 5 divided damage =====

    @Nested
    @DisplayName("ETB trigger")
    class ETBTrigger {

        @Test
        @DisplayName("ETB deals all 5 damage to a single player")
        void etbDeals5DamageToSinglePlayer() {
            harness.setLife(player2, 20);

            gd.pendingETBDamageAssignments = Map.of(player2.getId(), 5);

            castBogardanHellkite();
            harness.passBothPriorities(); // resolve creature spell
            harness.passBothPriorities(); // resolve ETB trigger

            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
        }

        @Test
        @DisplayName("ETB deals all 5 damage to a single creature, killing it")
        void etbDeals5DamageToSingleCreature() {
            Permanent cavalry = harness.addToBattlefieldAndReturn(player2, new BenalishCavalry());

            gd.pendingETBDamageAssignments = Map.of(cavalry.getId(), 5);

            castBogardanHellkite();
            harness.passBothPriorities(); // resolve creature spell
            harness.passBothPriorities(); // resolve ETB trigger

            harness.assertInGraveyard(player2, "Benalish Cavalry");
        }

        @Test
        @DisplayName("ETB divides damage among a creature and a player")
        void etbDividesDamageAmongCreatureAndPlayer() {
            harness.setLife(player2, 20);
            Permanent cavalry = harness.addToBattlefieldAndReturn(player2, new BenalishCavalry());

            gd.pendingETBDamageAssignments = Map.of(cavalry.getId(), 2, player2.getId(), 3);

            castBogardanHellkite();
            harness.passBothPriorities(); // resolve creature spell
            harness.passBothPriorities(); // resolve ETB trigger

            // Cavalry took 2 damage, which is lethal for a 2/2.
            harness.assertInGraveyard(player2, "Benalish Cavalry");

            // Player took 3 damage
            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        }

        @Test
        @DisplayName("ETB divides damage among both players and a creature")
        void etbDividesDamageAmongThreeTargets() {
            harness.setLife(player1, 20);
            harness.setLife(player2, 20);
            Permanent cavalry = harness.addToBattlefieldAndReturn(player2, new BenalishCavalry());

            gd.pendingETBDamageAssignments = Map.of(
                    cavalry.getId(), 1,
                    player1.getId(), 2,
                    player2.getId(), 2
            );

            castBogardanHellkite();
            harness.passBothPriorities(); // resolve creature spell
            harness.passBothPriorities(); // resolve ETB trigger

            assertThat(cavalry.getMarkedDamage()).isEqualTo(1);

            assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        }

        @Test
        @DisplayName("ETB can divide all 5 damage among five targets")
        void etbDividesDamageAmongFiveTargets() {
            List<Permanent> targets = List.of(
                    harness.addToBattlefieldAndReturn(player1, new BenalishCavalry()),
                    harness.addToBattlefieldAndReturn(player1, new BenalishCavalry()),
                    harness.addToBattlefieldAndReturn(player2, new BenalishCavalry()),
                    harness.addToBattlefieldAndReturn(player2, new BenalishCavalry()),
                    harness.addToBattlefieldAndReturn(player2, new BenalishCavalry())
            );

            gd.pendingETBDamageAssignments = Map.of(
                    targets.get(0).getId(), 1,
                    targets.get(1).getId(), 1,
                    targets.get(2).getId(), 1,
                    targets.get(3).getId(), 1,
                    targets.get(4).getId(), 1
            );

            castBogardanHellkite();
            harness.passBothPriorities(); // resolve creature spell
            harness.passBothPriorities(); // resolve ETB trigger

            for (Permanent target : targets) {
                assertThat(target.getMarkedDamage()).isEqualTo(1);
            }
        }

        @Test
        @DisplayName("ETB with no damage assignments does nothing")
        void etbWithNoDamageAssignments() {
            harness.setLife(player2, 20);

            gd.pendingETBDamageAssignments = Map.of();

            castBogardanHellkite();
            harness.passBothPriorities(); // resolve creature spell
            harness.passBothPriorities(); // resolve ETB trigger

            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        }
    }

    // ===== Helpers =====

    private void castBogardanHellkite() {
        harness.setHand(player1, List.of(new BogardanHellkite()));
        harness.addMana(player1, ManaColor.RED, 8);
        harness.castCreature(player1, 0);
    }
}
