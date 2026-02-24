package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.EachOpponentLosesLifeEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BlistergrubTest extends BaseCardTest {


    // ===== Card properties =====


    @Test
    @DisplayName("Blistergrub has ON_DEATH effect that makes each opponent lose 2 life")
    void hasDeathTriggerEffect() {
        Blistergrub card = new Blistergrub();

        assertThat(card.getEffects(EffectSlot.ON_DEATH)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_DEATH).getFirst())
                .isInstanceOf(EachOpponentLosesLifeEffect.class);
        EachOpponentLosesLifeEffect effect = (EachOpponentLosesLifeEffect) card.getEffects(EffectSlot.ON_DEATH).getFirst();
        assertThat(effect.amount()).isEqualTo(2);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Blistergrub puts it on the battlefield")
    void castingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new Blistergrub()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Blistergrub"));
    }

    // ===== Death trigger =====

    @Test
    @DisplayName("When Blistergrub dies in combat, death trigger goes on the stack")
    void deathTriggerGoesOnStack() {
        harness.addToBattlefield(player1, new Blistergrub());
        harness.setLife(player2, 20);

        setupCombatWhereBlistergrubDies();
        harness.passBothPriorities(); // Combat damage — Blistergrub dies

        // Blistergrub should be in graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Blistergrub"));

        // Death trigger should be on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Blistergrub");
    }

    @Test
    @DisplayName("Resolving death trigger causes each opponent to lose 2 life")
    void deathTriggerCausesOpponentLifeLoss() {
        harness.addToBattlefield(player1, new Blistergrub());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        setupCombatWhereBlistergrubDies();
        harness.passBothPriorities(); // Combat damage — Blistergrub dies

        // Resolve the death trigger
        harness.passBothPriorities();

        // Opponent loses 2 life: 20 - 2 = 18
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Controller does not lose life from Blistergrub's death trigger")
    void controllerDoesNotLoseLife() {
        harness.addToBattlefield(player1, new Blistergrub());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        setupCombatWhereBlistergrubDies();
        harness.passBothPriorities(); // Combat damage — Blistergrub dies

        // Resolve the death trigger
        harness.passBothPriorities();

        // Controller's life should not be affected by the trigger
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Death trigger life loss is logged")
    void deathTriggerLifeLossIsLogged() {
        harness.addToBattlefield(player1, new Blistergrub());
        harness.setLife(player2, 20);

        setupCombatWhereBlistergrubDies();
        harness.passBothPriorities(); // Combat damage — Blistergrub dies

        // Resolve the death trigger
        harness.passBothPriorities();

        assertThat(gd.gameLog).anyMatch(log -> log.contains("loses") && log.contains("2") && log.contains("life"));
    }

    // ===== Helpers =====

    /**
     * Sets up combat where Blistergrub (player1, 2/2) attacks and is blocked by a 3/3 creature (player2).
     * Blistergrub will die from combat damage.
     */
    private void setupCombatWhereBlistergrubDies() {
        Permanent blistergrubPerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Blistergrub"))
                .findFirst().orElseThrow();
        blistergrubPerm.setSummoningSick(false);
        blistergrubPerm.setAttacking(true);

        GrizzlyBears bigBear = new GrizzlyBears();
        bigBear.setPower(3);
        bigBear.setToughness(3);
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
