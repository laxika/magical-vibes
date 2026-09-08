package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.c.CarrierPigeons;
import com.github.laxika.magicalvibes.cards.w.WhipVine;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VarchildsCrusader.class, CarrierPigeons.class, WhipVine.class})
@DisplayName("Varchild's Crusader")
class VarchildsCrusaderTest extends BaseCardTest {

    @Test
    @DisplayName("A non-Wall creature can't block the Crusader after activation")
    void nonWallCreatureCannotBlock() {
        activateCrusader();

        addCreatureReady(player2, new CarrierPigeons());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Walls");
    }

    @Test
    @DisplayName("A Wall can still block the Crusader")
    void wallCanBlock() {
        activateCrusader();

        Permanent wall = addCreatureReady(player2, new WhipVine());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(wall.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("The blocking restriction wears off at the end of the turn")
    void restrictionWearsOffAtEndOfTurn() {
        Permanent crusader = addCreatureReady(player1, new VarchildsCrusader());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.CLEANUP);
        GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd);

        crusader.setAttacking(true);
        Permanent creature = addCreatureReady(player2, new CarrierPigeons());

        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(creature.isBlocking()).isTrue();
        harness.assertOnBattlefield(player1, "Varchild's Crusader");
    }

    @Test
    @DisplayName("The Crusader is sacrificed at the beginning of the next end step")
    void sacrificedAtNextEndStep() {
        Permanent crusader = activateCrusader();
        crusader.setAttacking(false);

        harness.assertOnBattlefield(player1, "Varchild's Crusader");

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Varchild's Crusader");
        harness.assertInGraveyard(player1, "Varchild's Crusader");
    }

    @Test
    @DisplayName("The delayed sacrifice does nothing if another player controls the Crusader")
    void delayedSacrificeCannotSacrificePermanentControlledByAnotherPlayer() {
        Permanent crusader = activateCrusader();
        crusader.setAttacking(false);

        gd.playerBattlefields.get(player1.getId()).remove(crusader);
        gd.playerBattlefields.get(player2.getId()).add(crusader);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Varchild's Crusader");
        harness.assertOnBattlefield(player2, "Varchild's Crusader");
    }

    /**
     * Puts the Crusader onto the battlefield, activates its {@code {0}} ability and resolves it,
     * leaving the Crusader attacking.
     */
    private Permanent activateCrusader() {
        Permanent crusader = addCreatureReady(player1, new VarchildsCrusader());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        crusader.setAttacking(true);
        return crusader;
    }
}
