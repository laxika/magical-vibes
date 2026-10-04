package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(GoblinWarBuggy.class)
class GoblinWarBuggyTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield does not put an echo registration trigger on the stack")
    void enteringDoesNotCreateATrigger() {
        harness.castFromHand(player1, new GoblinWarBuggy(), "{1}{R}");

        harness.withAutoStop(gd.currentStep, harness::passBothPriorities);

        harness.assertOnBattlefield(player1, "Goblin War Buggy");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent's upkeep does not consume the echo obligation")
    void echoStillTriggersAfterOpponentsUpkeep() {
        castAndResolveGoblinWarBuggy();

        advanceToUpkeep(player2);
        assertThat(gd.stack).isEmpty();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Goblin War Buggy");
        harness.assertInGraveyard(player1, "Goblin War Buggy");
    }

    @Test
    @DisplayName("Accepting echo without enough mana sacrifices Goblin War Buggy")
    void acceptingEchoWithoutEnoughManaSacrificesIt() {
        castAndResolveGoblinWarBuggy();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Goblin War Buggy");
        harness.assertInGraveyard(player1, "Goblin War Buggy");
    }

    @Test
    @DisplayName("Haste allows Goblin War Buggy to attack the turn it enters")
    void hasteAllowsAttackingTheTurnItEnters() {
        castAndResolveGoblinWarBuggy();

        declareAttackers(List.of(0));
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Declining echo sacrifices Goblin War Buggy at its next upkeep")
    void decliningEchoSacrificesGoblinWarBuggy() {
        castAndResolveGoblinWarBuggy();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Goblin War Buggy");
        harness.assertInGraveyard(player1, "Goblin War Buggy");
    }

    @Test
    @DisplayName("Paying {1}{R} for echo keeps Goblin War Buggy and echo does not trigger again")
    void payingEchoKeepsGoblinWarBuggyAndIsOneShot() {
        castAndResolveGoblinWarBuggy();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Goblin War Buggy");

        advanceToUpkeep(player1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Goblin War Buggy");
    }

    @Test
    @DisplayName("Echo does not trigger during an opponent's upkeep")
    void echoDoesNotTriggerDuringOpponentsUpkeep() {
        castAndResolveGoblinWarBuggy();

        advanceToUpkeep(player2);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Goblin War Buggy");
    }

    private void castAndResolveGoblinWarBuggy() {
        harness.castFromHand(player1, new GoblinWarBuggy(), "{1}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Goblin War Buggy");
    }
}
