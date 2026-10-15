package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({InfernoTitan.class, RuneclawBear.class, Unsummon.class})
class InfernoTitanTest extends BaseCardTest {

    @Nested
    @DisplayName("ETB trigger")
    @CardUsed({InfernoTitan.class, RuneclawBear.class, Unsummon.class})
    class ETBTrigger {

        @Test
        @DisplayName("ETB deals all 3 damage to a single creature target")
        void etbDeals3DamageToSingleCreature() {
            harness.addToBattlefield(player2, new RuneclawBear());
            UUID bearsId = harness.getPermanentId(player2, "Runeclaw Bear");

            gd.pendingETBDamageAssignments = Map.of(bearsId, 3);

            castInfernoTitan();
            harness.passBothPriorities(); // resolve creature spell
            harness.passBothPriorities(); // resolve ETB trigger

            // Runeclaw Bear (2/2) should be dead from 3 damage
            harness.assertInGraveyard(player2, "Runeclaw Bear");
        }

        @Test
        @DisplayName("ETB deals all 3 damage to a player")
        void etbDeals3DamageToPlayer() {
            harness.setLife(player2, 20);

            gd.pendingETBDamageAssignments = Map.of(player2.getId(), 3);

            castInfernoTitan();
            harness.passBothPriorities(); // resolve creature spell
            harness.passBothPriorities(); // resolve ETB trigger

            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        }

        @Test
        @DisplayName("ETB divides damage among two targets")
        void etbDividesDamageAmongTwoTargets() {
            harness.setLife(player2, 20);
            harness.addToBattlefield(player2, new RuneclawBear());
            UUID bearsId = harness.getPermanentId(player2, "Runeclaw Bear");

            gd.pendingETBDamageAssignments = Map.of(bearsId, 1, player2.getId(), 2);

            castInfernoTitan();
            harness.passBothPriorities(); // resolve creature spell
            harness.passBothPriorities(); // resolve ETB trigger

            // Bears took 1 damage (alive at 2/2 with 1 damage)
            Permanent bears = findPermanent(player2, "Runeclaw Bear");
            assertThat(bears.getMarkedDamage()).isEqualTo(1);

            // Player took 2 damage
            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        }

        @Test
        @DisplayName("ETB divides damage among three targets")
        void etbDividesDamageAmongThreeTargets() {
            harness.setLife(player1, 20);
            harness.setLife(player2, 20);
            harness.addToBattlefield(player2, new RuneclawBear());
            UUID bearsId = harness.getPermanentId(player2, "Runeclaw Bear");

            gd.pendingETBDamageAssignments = Map.of(
                    bearsId, 1,
                    player1.getId(), 1,
                    player2.getId(), 1
            );

            castInfernoTitan();
            harness.passBothPriorities(); // resolve creature spell
            harness.passBothPriorities(); // resolve ETB trigger

            Permanent bears = findPermanent(player2, "Runeclaw Bear");
            assertThat(bears.getMarkedDamage()).isEqualTo(1);

            assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        }

        @Test
        @CardUsed({InfernoTitan.class, RuneclawBear.class, Unsummon.class})
        @DisplayName("ETB preserves the surviving target's assigned damage when another target leaves")
        void etbDoesNotRedistributeDamageFromMissingTarget() {
            Permanent bear = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
            gd.pendingETBDamageAssignments = Map.of(bear.getId(), 2, player2.getId(), 1);
            castInfernoTitan();
            harness.passBothPriorities();

            harness.setHand(player2, List.of(new Unsummon()));
            harness.addMana(player2, ManaColor.BLUE, 1);
            harness.castAndResolveInstant(player2, 0, bear.getId());
            harness.passBothPriorities();

            harness.assertInHand(player2, "Runeclaw Bear");
            harness.assertLife(player2, 19);
        }

        @Test
        @CardUsed({InfernoTitan.class, Unsummon.class})
        @DisplayName("ETB still deals damage after Inferno Titan leaves the battlefield")
        void etbSurvivesSourceLeaving() {
            gd.pendingETBDamageAssignments = Map.of(player2.getId(), 3);
            castInfernoTitan();
            harness.passBothPriorities();
            UUID titanId = harness.getPermanentId(player1, "Inferno Titan");

            harness.setHand(player2, List.of(new Unsummon()));
            harness.addMana(player2, ManaColor.BLUE, 1);
            harness.castAndResolveInstant(player2, 0, titanId);
            harness.passBothPriorities();

            harness.assertInHand(player1, "Inferno Titan");
            harness.assertLife(player2, 17);
        }

        @Test
        @DisplayName("ETB requires targets and a damage division before priority resumes")
        void etbRequiresTargetSelection() {
            castInfernoTitan();
            harness.passBothPriorities();

            assertThat(gd.interaction.isAwaitingInput()).isTrue();
            harness.assertLife(player2, 20);
        }

    }

    @Nested
    @DisplayName("Attack trigger")
    @CardUsed({InfernoTitan.class, RuneclawBear.class})
    class AttackTrigger {

        @Test
        @DisplayName("Attacking requires target and damage division selection")
        void attackRequiresTargetSelection() {
            addCreatureReady(player1, new InfernoTitan());
            declareAttackers(List.of(0));

            assertThat(gd.interaction.isAwaitingInput()).isTrue();
            harness.assertLife(player2, 20);
        }

        @Test
        @DisplayName("Attacking deals 3 trigger damage plus 6 combat damage to defender")
        void attackDeals3DamageToSingleTarget() {
            harness.setLife(player1, 20);
            harness.setLife(player2, 20);

            addCreatureReady(player1, new InfernoTitan());

            gd.pendingETBDamageAssignments = Map.of(player2.getId(), 3);

            declareAttackers(List.of(0));
            harness.passBothPriorities(); // resolves trigger + auto-passes through combat

            // 3 from trigger + 6 from combat damage = 9 total
            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(11);
        }

        @Test
        @DisplayName("Attack trigger kills creature, then combat damage hits player")
        void attackDividesDamage() {
            harness.setLife(player1, 20);
            harness.setLife(player2, 20);
            harness.addToBattlefield(player2, new RuneclawBear());
            UUID bearsId = harness.getPermanentId(player2, "Runeclaw Bear");

            addCreatureReady(player1, new InfernoTitan());

            gd.pendingETBDamageAssignments = Map.of(bearsId, 2, player2.getId(), 1);

            declareAttackers(List.of(0));
            harness.passBothPriorities(); // resolves trigger + auto-passes through combat

            // Bears took 2 damage (lethal for 2/2) — should be dead
            harness.assertInGraveyard(player2, "Runeclaw Bear");

            // 1 from trigger + 6 from combat (unblocked) = 7 total
            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(13);
        }
    }

    @Test
    @DisplayName("Attack trigger retains the damage division announced when it triggered")
    void attackRetainsAnnouncedDivision() {
        addCreatureReady(player1, new InfernoTitan());
        gd.pendingETBDamageAssignments = Map.of(player2.getId(), 3);
        declareAttackers(List.of(0));

        // Another ability's pending choice must not replace this trigger's announced division.
        gd.pendingETBDamageAssignments = Map.of(player1.getId(), 3);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 11);
    }

    @Nested
    @DisplayName("Firebreathing ability")
    @CardUsed({InfernoTitan.class})
    class Firebreathing {

        @Test
        @DisplayName("{R}: gives +1/+0 until end of turn")
        void firebreathingBoosts() {
            harness.setLife(player1, 20);
            harness.setLife(player2, 20);

            Permanent infernoTitan = addCreatureReady(player1, new InfernoTitan());

            harness.addMana(player1, ManaColor.RED, 3);
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities(); // resolve ability

            assertThat(infernoTitan.getPowerModifier()).isEqualTo(1);
        }

        @Test
        @DisplayName("Firebreathing expires during cleanup and does not boost toughness")
        void firebreathingExpiresAtCleanup() {
            Permanent titan = addCreatureReady(player1, new InfernoTitan());
            harness.addMana(player1, ManaColor.RED, 1);
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
            assertThat(titan.getPowerModifier()).isEqualTo(1);
            assertThat(titan.getToughnessModifier()).isZero();

            harness.forceStep(TurnStep.END_STEP);
            harness.clearPriorityPassed();
            harness.passBothPriorities();
            assertThat(titan.getPowerModifier()).isZero();
            assertThat(titan.getToughnessModifier()).isZero();
        }

        @Test
        @DisplayName("Firebreathing requires red mana")
        void firebreathingRequiresRedMana() {
            addCreatureReady(player1, new InfernoTitan());
            harness.addMana(player1, ManaColor.GREEN, 1);

            assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Not enough mana");
        }

        @Test
        @DisplayName("Activating firebreathing multiple times stacks")
        void firebreathingStacks() {
            harness.setLife(player1, 20);
            harness.setLife(player2, 20);

            Permanent infernoTitan = addCreatureReady(player1, new InfernoTitan());

            harness.addMana(player1, ManaColor.RED, 3);

            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();

            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();

            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();

            assertThat(infernoTitan.getPowerModifier()).isEqualTo(3);
        }
    }

    private void castInfernoTitan() {
        harness.setHand(player1, List.of(new InfernoTitan()));
        harness.addMana(player1, ManaColor.RED, 6);
        harness.castCreature(player1, 0);
    }

}
