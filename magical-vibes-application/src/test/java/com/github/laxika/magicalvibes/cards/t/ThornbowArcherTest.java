package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.e.ElvishVisionary;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThornbowArcher.class, ElvishVisionary.class})
class ThornbowArcherTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking drains an opponent who controls no Elf")
    void opponentWithoutElfLosesLife() {
        addCreatureReady(player1, new ThornbowArcher());

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        // 1 from the trigger + 1 combat damage (power 1).
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("An opponent controlling an Elf loses no life to the trigger")
    void opponentWithElfIsExempt() {
        addCreatureReady(player1, new ThornbowArcher());
        harness.addToBattlefield(player2, new ElvishVisionary());

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        // The Visionary is a possible blocker, so combat waits for a blocker declaration instead of
        // auto-passing to damage the way the no-blocker tests above do.
        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();

        // Only combat damage; the trigger skips the Elf controller.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("The controller's own Elf does not exempt the opponent")
    void controllerElfDoesNotMatter() {
        addCreatureReady(player1, new ThornbowArcher());
        harness.addToBattlefield(player1, new ElvishVisionary());

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("The attacking player never loses life to their own trigger")
    void controllerNeverLosesLife() {
        addCreatureReady(player1, new ThornbowArcher());

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("An Elf entering after the attack exempts its controller at resolution")
    void elfEnteringBeforeResolutionExemptsOpponent() {
        addCreatureReady(player1, new ThornbowArcher());
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player1, List.of(0));
            assertThat(gd.stack).hasSize(1);
            harness.addToBattlefield(player2, new ElvishVisionary());
            resolveAllTriggers();

            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
        });
    }

    @Test
    @DisplayName("An Elf leaving after the attack does not exempt its controller at resolution")
    void elfLeavingBeforeResolutionMakesOpponentLoseLife() {
        addCreatureReady(player1, new ThornbowArcher());
        harness.addToBattlefield(player2, new ElvishVisionary());
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player1, List.of(0));
            assertThat(gd.stack).hasSize(1);
            var elf = gd.playerBattlefields.get(player2.getId()).removeFirst();
            gd.playerGraveyards.get(player2.getId()).add(elf.getCard());
            resolveAllTriggers();

            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 1);
        });
    }
}
