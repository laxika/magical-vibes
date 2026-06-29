package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.EachPlayerLosesLifeEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CausticHoundTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Caustic Hound has ON_DEATH effect that makes each player lose 4 life")
    void hasDeathTriggerEffect() {
        CausticHound card = new CausticHound();

        assertThat(card.getEffects(EffectSlot.ON_DEATH)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_DEATH).getFirst())
                .isInstanceOf(EachPlayerLosesLifeEffect.class);
        EachPlayerLosesLifeEffect effect = (EachPlayerLosesLifeEffect) card.getEffects(EffectSlot.ON_DEATH).getFirst();
        assertThat(effect.amount()).isEqualTo(4);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Caustic Hound puts it on the battlefield")
    void castingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new CausticHound()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Caustic Hound"));
    }

    // ===== Death trigger =====

    @Test
    @DisplayName("When Caustic Hound dies in combat, death trigger goes on the stack")
    void deathTriggerGoesOnStack() {
        harness.addToBattlefield(player1, new CausticHound());
        harness.setLife(player2, 20);

        setupCombatWhereCausticHoundDies();
        harness.passBothPriorities(); // Combat damage — Caustic Hound dies

        // Caustic Hound should be in graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Caustic Hound"));

        // Death trigger should be on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Caustic Hound");
    }

    @Test
    @DisplayName("Resolving death trigger causes each player to lose 4 life")
    void deathTriggerCausesEachPlayerLifeLoss() {
        harness.addToBattlefield(player1, new CausticHound());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        setupCombatWhereCausticHoundDies();
        harness.passBothPriorities(); // Combat damage — Caustic Hound dies

        // Resolve the death trigger
        harness.passBothPriorities();

        // Both players lose 4 life
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(16);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Controller also loses life from Caustic Hound's death trigger")
    void controllerAlsoLosesLife() {
        harness.addToBattlefield(player1, new CausticHound());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        setupCombatWhereCausticHoundDies();
        harness.passBothPriorities(); // Combat damage — Caustic Hound dies

        // Resolve the death trigger
        harness.passBothPriorities();

        // Controller loses 4 life: 20 - 4 = 16
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Death trigger life loss is logged")
    void deathTriggerLifeLossIsLogged() {
        harness.addToBattlefield(player1, new CausticHound());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        setupCombatWhereCausticHoundDies();
        harness.passBothPriorities(); // Combat damage — Caustic Hound dies

        // Resolve the death trigger
        harness.passBothPriorities();

        assertThat(gd.gameLog).anyMatch(log -> log.contains("loses") && log.contains("4") && log.contains("life"));
    }

    // ===== Helpers =====

    /**
     * Sets up combat where Caustic Hound (player1, 4/4) attacks and is blocked by a 5/5 creature (player2).
     * Caustic Hound will die from combat damage.
     */
    private void setupCombatWhereCausticHoundDies() {
        Permanent causticHoundPerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Caustic Hound"))
                .findFirst().orElseThrow();
        causticHoundPerm.setSummoningSick(false);
        causticHoundPerm.setAttacking(true);

        GrizzlyBears bigBear = new GrizzlyBears();
        bigBear.setPower(5);
        bigBear.setToughness(5);
        Permanent blockerPerm = new Permanent(bigBear);
        blockerPerm.setSummoningSick(false);
        blockerPerm.setBlocking(true);
        blockerPerm.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(blockerPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
    }
}
