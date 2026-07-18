package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WallOfAir;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.turn.StepTriggerService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SirensCallTest extends BaseCardTest {

    /** player1 (caster) holds Siren's Call with mana; it's player2's turn, before attackers. */
    private void primeCall() {
        harness.forceActivePlayer(player2);
        harness.setHand(player1, List.of(new SirensCall()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
    }

    private void runEndStep() {
        harness.forceStep(TurnStep.END_STEP);
        GameTestEngineContext.get().getBean(StepTriggerService.class).handleEndStepTriggers(gd);
    }

    @Test
    @DisplayName("Forces the active player's creatures to attack this turn if able")
    void forcesActivePlayersCreaturesToAttack() {
        Permanent bear = addCreatureReady(player2, new GrizzlyBears());
        primeCall();

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(bear.isMustAttackThisTurn()).isTrue();
    }

    @Test
    @DisplayName("At end step, destroys non-Wall creatures that didn't attack; spares Walls, attackers, and newly-controlled creatures")
    void destroysNonAttackersAtEndStep() {
        Permanent lazy = addCreatureReady(player2, new GrizzlyBears());
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttackedThisTurn(true);
        Permanent wall = addCreatureReady(player2, new WallOfAir());
        // Came under control this turn (summoning sick) => didn't control it since the turn began.
        Permanent fresh = new Permanent(new GrizzlyBears());
        fresh.setSummoningSick(true);
        gd.playerBattlefields.get(player2.getId()).add(fresh);

        primeCall();
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        runEndStep();

        // Only the ready non-Wall creature that didn't attack is destroyed.
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .doesNotContain(lazy)
                .contains(attacker, wall, fresh);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Cannot be cast during your own turn")
    void cannotCastOnYourOwnTurn() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new SirensCall()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Cannot be cast once attackers are declared")
    void cannotCastAfterAttackersDeclared() {
        harness.forceActivePlayer(player2);
        harness.setHand(player1, List.of(new SirensCall()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }
}
