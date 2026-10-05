package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KratosGodOfWar.class, GrizzlyBears.class})
class KratosGodOfWarTest extends BaseCardTest {

    @Test
    @DisplayName("All creatures have haste")
    void grantsHasteToAllCreatures() {
        harness.addToBattlefield(player1, new KratosGodOfWar());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Grizzly Bears"), Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, findPermanent(player2, "Grizzly Bears"), Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Deals damage equal to the end-step player's creatures that did not attack")
    void dealsDamageForCreaturesThatDidNotAttack() {
        harness.addToBattlefield(player2, new KratosGodOfWar());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLife(player1, 20);

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat(player1);
        resolveEndStep(player1);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Damages the player whose end step it is")
    void damagesEndStepPlayerNotKratosController() {
        harness.addToBattlefield(player2, new KratosGodOfWar());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        resolveEndStep(player1);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    private void resolveEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.END_STEP);
        resolveAllTriggers();
    }

    @Test
    @DisplayName("Kratos counts itself on its controller's end step if it did not attack")
    void countsItselfOnControllersEndStep() {
        harness.addToBattlefield(player1, new KratosGodOfWar());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLife(player1, 20);

        resolveEndStep(player1);

        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("An empty battlefield causes no end-step damage")
    void noDamageWithoutCreatures() {
        harness.addToBattlefield(player2, new KratosGodOfWar());
        harness.setLife(player1, 20);

        resolveEndStep(player1);

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Kratos can attack immediately, deals double-strike damage, and is not counted afterward")
    void attacksWithHasteAndDoubleStrike() {
        harness.addToBattlefield(player1, new KratosGodOfWar());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat(player1);

        harness.assertLife(player2, 16);

        resolveEndStep(player1);

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("A creature entering after combat is counted even though it could not have attacked")
    void countsCreatureEnteringAfterCombat() {
        harness.addToBattlefield(player2, new KratosGodOfWar());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLife(player1, 20);

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        resolveEndStep(player1);

        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("Creature count is evaluated when the end-step trigger resolves")
    void countsCreaturesAtResolution() {
        harness.addToBattlefield(player2, new KratosGodOfWar());
        harness.setLife(player1, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.withAutoStop(TurnStep.END_STEP,
                () -> harness.passUntil(player1, TurnStep.END_STEP));

        harness.addToBattlefield(player1, new GrizzlyBears());
        resolveAllTriggers();

        harness.assertLife(player1, 19);
    }
}
