package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CountrysideCrusher;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TheGitrogMonsterTest extends BaseCardTest {

    private void drainStack() {
        int guard = 0;
        while (!gd.stack.isEmpty() && guard++ < 50) {
            harness.passBothPriorities();
        }
    }

    private int handSize() {
        return gd.playerHands.get(player1.getId()).size();
    }

    @Test
    @DisplayName("Upkeep with no land sacrifices The Gitrog Monster without prompting")
    void upkeepWithoutLandSacrificesItself() {
        harness.addToBattlefield(player1, new TheGitrogMonster());

        advanceToUpkeep(player1);
        drainStack();

        harness.assertNotOnBattlefield(player1, "The Gitrog Monster");
        harness.assertInGraveyard(player1, "The Gitrog Monster");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Sacrificing a land at upkeep keeps it and draws a card for the binned land")
    void sacrificingLandKeepsItAndDraws() {
        harness.addToBattlefield(player1, new TheGitrogMonster());
        harness.addToBattlefield(player1, new Forest());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve the upkeep trigger

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, findPermanent(player1, "Forest").getId());

        int handBefore = handSize();
        drainStack();

        harness.assertOnBattlefield(player1, "The Gitrog Monster");
        harness.assertInGraveyard(player1, "Forest");
        assertThat(handSize()).isEqualTo(handBefore + 1);
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining to sacrifice a land sacrifices The Gitrog Monster")
    void decliningSacrificesItself() {
        harness.addToBattlefield(player1, new TheGitrogMonster());
        harness.addToBattlefield(player1, new Forest());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);
        drainStack();

        harness.assertNotOnBattlefield(player1, "The Gitrog Monster");
        harness.assertInGraveyard(player1, "The Gitrog Monster");
        harness.assertOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("Controller may play one additional land each turn; opponents may not")
    void grantsControllerOnlyExtraLandPlay() {
        harness.addToBattlefield(player1, new TheGitrogMonster());

        assertThat(gd.getMaxLandsThisTurn(player1.getId())).isEqualTo(2);
        assertThat(gd.getMaxLandsThisTurn(player2.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("The controller can actually play two lands in one turn")
    void controllerPlaysTwoLandsInOneTurn() {
        harness.addToBattlefield(player1, new TheGitrogMonster());
        harness.setHand(player1, List.of(new Forest(), new Forest()));

        harness.playLand(player1, 0);
        harness.playLand(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> "Forest".equals(p.getCard().getName()))
                .count()).isEqualTo(2);
    }

    @Test
    @DisplayName("A land milled into the graveyard from the library draws a card")
    void landMilledFromLibraryDrawsCard() {
        harness.addToBattlefield(player1, new TheGitrogMonster());
        harness.addToBattlefield(player1, new CountrysideCrusher());
        harness.addToBattlefield(player1, new Forest());
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears()));

        advanceToUpkeep(player1);

        // Both upkeep triggers are on the stack; pay Gitrog's by sacrificing the battlefield
        // Forest whenever its prompt surfaces, whichever order the two resolve in.
        int guard = 0;
        while ((!gd.stack.isEmpty() || gd.interaction.activeInteraction() != null) && guard++ < 50) {
            if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
                harness.handleMayAbilityChosen(player1, true);
                harness.handlePermanentChosen(player1, findPermanent(player1, "Forest").getId());
                continue;
            }
            harness.passBothPriorities();
        }

        // The sacrificed Forest and the Forest Countryside Crusher binned each drew a card.
        harness.assertOnBattlefield(player1, "The Gitrog Monster");
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        harness.assertInHand(player1, "Grizzly Bears");
    }
}
