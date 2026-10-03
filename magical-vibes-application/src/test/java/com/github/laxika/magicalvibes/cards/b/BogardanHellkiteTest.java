package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GarrukWildspeaker;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({BogardanHellkite.class, RuneclawBear.class, GarrukWildspeaker.class, Unsummon.class})
class BogardanHellkiteTest extends BaseCardTest {

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

    @CardUsed({BogardanHellkite.class, RuneclawBear.class, GarrukWildspeaker.class, Unsummon.class})
    @Nested
    @DisplayName("ETB trigger")
    class ETBTrigger {

        @Test
        @DisplayName("ETB asks for targets and damage division before players can respond")
        void etbRequestsDamageDivisionWhenEnteringNormally() {
            castBogardanHellkite();
            harness.passBothPriorities();

            assertThat(gd.interaction.isAwaitingInput()).isTrue();
            harness.assertLife(player1, 20);
            harness.assertLife(player2, 20);
        }

        @Test
        @DisplayName("ETB damage removes loyalty from a planeswalker")
        void etbDealsDamageToPlaneswalker() {
            Permanent garruk = harness.addToBattlefieldAndReturn(player2, new GarrukWildspeaker());
            garruk.setCounterCount(CounterType.LOYALTY, 3);
            gd.pendingETBDamageAssignments = Map.of(garruk.getId(), 2, player2.getId(), 3);

            castBogardanHellkite();
            harness.passBothPriorities();
            harness.passBothPriorities();

            assertThat(garruk.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
            harness.assertOnBattlefield(player2, "Garruk Wildspeaker");
            harness.assertLife(player2, 17);
        }

        @Test
        @DisplayName("Damage assigned to a removed target is lost rather than redistributed")
        void etbDoesNotRedistributeDamageWhenTargetLeaves() {
            Permanent bear = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
            gd.pendingETBDamageAssignments = Map.of(bear.getId(), 2, player2.getId(), 3);
            castBogardanHellkite();
            harness.passBothPriorities();

            harness.setHand(player2, List.of(new Unsummon()));
            harness.addMana(player2, ManaColor.BLUE, 1);
            harness.castInstant(player2, 0, bear.getId());
            harness.passBothPriorities();
            harness.passBothPriorities();

            harness.assertInHand(player2, "Runeclaw Bear");
            harness.assertLife(player2, 17);
        }

        @Test
        @DisplayName("ETB damage still happens when Hellkite leaves before resolution")
        void etbResolvesAfterSourceLeaves() {
            gd.pendingETBDamageAssignments = Map.of(player2.getId(), 5);
            castBogardanHellkite();
            harness.passBothPriorities();

            harness.setHand(player2, List.of(new Unsummon()));
            harness.addMana(player2, ManaColor.BLUE, 1);
            harness.castInstant(player2, 0, harness.getPermanentId(player1, "Bogardan Hellkite"));
            harness.passBothPriorities();
            harness.passBothPriorities();

            harness.assertInHand(player1, "Bogardan Hellkite");
            harness.assertLife(player2, 15);
        }

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
            Permanent bear = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());

            gd.pendingETBDamageAssignments = Map.of(bear.getId(), 5);

            castBogardanHellkite();
            harness.passBothPriorities(); // resolve creature spell
            harness.passBothPriorities(); // resolve ETB trigger

            harness.assertInGraveyard(player2, "Runeclaw Bear");
        }

        @Test
        @DisplayName("ETB divides damage among a creature and a player")
        void etbDividesDamageAmongCreatureAndPlayer() {
            harness.setLife(player2, 20);
            Permanent bear = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());

            gd.pendingETBDamageAssignments = Map.of(bear.getId(), 2, player2.getId(), 3);

            castBogardanHellkite();
            harness.passBothPriorities(); // resolve creature spell
            harness.passBothPriorities(); // resolve ETB trigger

            // Bear took 2 damage, which is lethal for a 2/2.
            harness.assertInGraveyard(player2, "Runeclaw Bear");

            // Player took 3 damage
            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        }

        @Test
        @DisplayName("ETB divides damage among both players and a creature")
        void etbDividesDamageAmongThreeTargets() {
            harness.setLife(player1, 20);
            harness.setLife(player2, 20);
            Permanent bear = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());

            gd.pendingETBDamageAssignments = Map.of(
                    bear.getId(), 1,
                    player1.getId(), 2,
                    player2.getId(), 2
            );

            castBogardanHellkite();
            harness.passBothPriorities(); // resolve creature spell
            harness.passBothPriorities(); // resolve ETB trigger

            assertThat(bear.getMarkedDamage()).isEqualTo(1);

            assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        }

        @Test
        @DisplayName("ETB can divide all 5 damage among five targets")
        void etbDividesDamageAmongFiveTargets() {
            List<Permanent> targets = List.of(
                    harness.addToBattlefieldAndReturn(player1, new RuneclawBear()),
                    harness.addToBattlefieldAndReturn(player1, new RuneclawBear()),
                    harness.addToBattlefieldAndReturn(player2, new RuneclawBear()),
                    harness.addToBattlefieldAndReturn(player2, new RuneclawBear()),
                    harness.addToBattlefieldAndReturn(player2, new RuneclawBear())
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

    private void castBogardanHellkite() {
        harness.castFromHand(player1, new BogardanHellkite(), "{6}{R}{R}");
    }
}
