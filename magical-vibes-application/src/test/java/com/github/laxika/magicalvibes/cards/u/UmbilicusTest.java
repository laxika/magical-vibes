package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GorillaWarrior;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Umbilicus.class, Forest.class, GorillaWarrior.class})
class UmbilicusTest extends BaseCardTest {

    @Test
    @DisplayName("The active player may pay 2 life to keep their permanents")
    void activePlayerMayPayLife() {
        harness.addToBattlefield(player1, new Umbilicus());
        harness.addToBattlefield(player2, new GorillaWarrior());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);

        harness.assertLife(player2, 18);
        harness.assertOnBattlefield(player2, "Gorilla Warrior");
    }

    @Test
    @DisplayName("The controller may pay 2 life during their own upkeep")
    void controllerMayPayLifeDuringOwnUpkeep() {
        harness.addToBattlefield(player1, new Umbilicus());
        harness.addToBattlefield(player1, new GorillaWarrior());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 18);
        harness.assertOnBattlefield(player1, "Gorilla Warrior");
    }

    @Test
    @DisplayName("If the active player declines, they choose a permanent they control to return")
    void decliningPaymentReturnsChosenPermanent() {
        harness.addToBattlefield(player1, new Umbilicus());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent gorilla = harness.addToBattlefieldAndReturn(player2, new GorillaWarrior());

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(forest.getId(), gorilla.getId());
        harness.handlePermanentChosen(player2, forest.getId());

        harness.assertInHand(player2, "Forest");
        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertOnBattlefield(player2, "Gorilla Warrior");
        harness.assertOnBattlefield(player1, "Umbilicus");
    }

    @Test
    @DisplayName("A player who cannot pay 2 life still resolves the return fallback")
    void cannotPayLifeReturnsPermanent() {
        harness.addToBattlefield(player1, new Umbilicus());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setLife(player2, 1);

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player2, forest.getId());

        harness.assertLife(player2, 1);
        harness.assertInHand(player2, "Forest");
        harness.assertNotOnBattlefield(player2, "Forest");
    }

    @Test
    @DisplayName("Declining with no permanent to return does nothing")
    void decliningWithNoPermanentDoesNothing() {
        harness.addToBattlefield(player1, new Umbilicus());

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        harness.assertLife(player2, 20);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Umbilicus");
    }
}
