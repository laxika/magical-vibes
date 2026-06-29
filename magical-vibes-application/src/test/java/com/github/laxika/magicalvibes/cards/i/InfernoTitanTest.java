package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.DealDividedDamageToAnyTargetsEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class InfernoTitanTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Inferno Titan has ON_ENTER_BATTLEFIELD DealDividedDamageToAnyTargetsEffect(3, 3)")
    void hasETBEffect() {
        InfernoTitan card = new InfernoTitan();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(DealDividedDamageToAnyTargetsEffect.class);
        DealDividedDamageToAnyTargetsEffect effect =
                (DealDividedDamageToAnyTargetsEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(effect.totalDamage()).isEqualTo(3);
        assertThat(effect.maxTargets()).isEqualTo(3);
    }

    @Test
    @DisplayName("Inferno Titan has ON_ATTACK DealDividedDamageToAnyTargetsEffect(3, 3)")
    void hasAttackEffect() {
        InfernoTitan card = new InfernoTitan();

        assertThat(card.getEffects(EffectSlot.ON_ATTACK)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ATTACK).getFirst())
                .isInstanceOf(DealDividedDamageToAnyTargetsEffect.class);
    }

    @Test
    @DisplayName("Inferno Titan has {R}: +1/+0 activated ability")
    void hasFirebreathingAbility() {
        InfernoTitan card = new InfernoTitan();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().getFirst().getEffects().getFirst())
                .isInstanceOf(BoostSelfEffect.class);
    }

    // ===== ETB trigger: deal 3 divided damage =====

    @Nested
    @DisplayName("ETB trigger")
    class ETBTrigger {

        @Test
        @DisplayName("ETB deals all 3 damage to a single creature target")
        void etbDeals3DamageToSingleCreature() {
            harness.addToBattlefield(player2, new GrizzlyBears());
            UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

            gd.pendingETBDamageAssignments = Map.of(bearsId, 3);

            castInfernoTitan();
            harness.passBothPriorities(); // resolve creature spell
            harness.passBothPriorities(); // resolve ETB trigger

            // Grizzly Bears (2/2) should be dead from 3 damage
            assertThat(gd.playerGraveyards.get(player2.getId()))
                    .anyMatch(c -> c.getName().equals("Grizzly Bears"));
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
            harness.addToBattlefield(player2, new GrizzlyBears());
            UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

            gd.pendingETBDamageAssignments = Map.of(bearsId, 1, player2.getId(), 2);

            castInfernoTitan();
            harness.passBothPriorities(); // resolve creature spell
            harness.passBothPriorities(); // resolve ETB trigger

            // Bears took 1 damage (alive at 2/2 with 1 damage)
            Permanent bears = gd.playerBattlefields.get(player2.getId()).stream()
                    .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                    .findFirst().orElse(null);
            assertThat(bears).isNotNull();
            assertThat(bears.getMarkedDamage()).isEqualTo(1);

            // Player took 2 damage
            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        }

        @Test
        @DisplayName("ETB divides damage among three targets")
        void etbDividesDamageAmongThreeTargets() {
            harness.setLife(player1, 20);
            harness.setLife(player2, 20);
            harness.addToBattlefield(player2, new GrizzlyBears());
            UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

            gd.pendingETBDamageAssignments = Map.of(
                    bearsId, 1,
                    player1.getId(), 1,
                    player2.getId(), 1
            );

            castInfernoTitan();
            harness.passBothPriorities(); // resolve creature spell
            harness.passBothPriorities(); // resolve ETB trigger

            Permanent bears = gd.playerBattlefields.get(player2.getId()).stream()
                    .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                    .findFirst().orElse(null);
            assertThat(bears).isNotNull();
            assertThat(bears.getMarkedDamage()).isEqualTo(1);

            assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        }

        @Test
        @DisplayName("ETB with no damage assignments does nothing")
        void etbWithNoDamageAssignments() {
            harness.setLife(player2, 20);

            gd.pendingETBDamageAssignments = Map.of();

            castInfernoTitan();
            harness.passBothPriorities(); // resolve creature spell
            harness.passBothPriorities(); // resolve ETB trigger

            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        }
    }

    // ===== Attack trigger: deal 3 divided damage =====

    @Nested
    @DisplayName("Attack trigger")
    class AttackTrigger {

        @Test
        @DisplayName("Attacking deals 3 trigger damage plus 6 combat damage to defender")
        void attackDeals3DamageToSingleTarget() {
            harness.setLife(player1, 20);
            harness.setLife(player2, 20);

            addReadyInfernoTitan(player1);

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
            harness.addToBattlefield(player2, new GrizzlyBears());
            UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

            addReadyInfernoTitan(player1);

            gd.pendingETBDamageAssignments = Map.of(bearsId, 2, player2.getId(), 1);

            declareAttackers(List.of(0));
            harness.passBothPriorities(); // resolves trigger + auto-passes through combat

            // Bears took 2 damage (lethal for 2/2) — should be dead
            assertThat(gd.playerGraveyards.get(player2.getId()))
                    .anyMatch(c -> c.getName().equals("Grizzly Bears"));

            // 1 from trigger + 6 from combat (unblocked) = 7 total
            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(13);
        }
    }

    // ===== Firebreathing: {R}: +1/+0 =====

    @Nested
    @DisplayName("Firebreathing ability")
    class Firebreathing {

        @Test
        @DisplayName("{R}: gives +1/+0 until end of turn")
        void firebreathingBoosts() {
            harness.setLife(player1, 20);
            harness.setLife(player2, 20);

            Permanent infernoTitan = addReadyInfernoTitan(player1);

            harness.addMana(player1, ManaColor.RED, 3);
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities(); // resolve ability

            assertThat(infernoTitan.getPowerModifier()).isEqualTo(1);
        }

        @Test
        @DisplayName("Activating firebreathing multiple times stacks")
        void firebreathingStacks() {
            harness.setLife(player1, 20);
            harness.setLife(player2, 20);

            Permanent infernoTitan = addReadyInfernoTitan(player1);

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

    // ===== Helpers =====

    private void castInfernoTitan() {
        harness.setHand(player1, List.of(new InfernoTitan()));
        harness.addMana(player1, ManaColor.RED, 6);
        harness.castCreature(player1, 0);
    }

    private Permanent addReadyInfernoTitan(Player player) {
        Permanent perm = new Permanent(new InfernoTitan());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void declareAttackers(List<Integer> attackerIndices) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);
        gs.declareAttackers(gd, player1, attackerIndices);
    }
}
