package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class BogElementalTest extends BaseCardTest {

    @Test
    @DisplayName("Auto-sacrifices when its controller has no land")
    void autoSacrificesWithoutLand() {
        harness.addToBattlefield(player1, new BogElemental());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Bog Elemental");
        harness.assertInGraveyard(player1, "Bog Elemental");
    }

    @Test
    @DisplayName("Sacrificing a land keeps Bog Elemental")
    void sacrificingLandKeepsElemental() {
        harness.addToBattlefield(player1, new BogElemental());
        harness.addToBattlefield(player1, new Forest());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        UUID forestId = findPermanent(player1, "Forest").getId();
        harness.handlePermanentChosen(player1, forestId);

        harness.assertOnBattlefield(player1, "Bog Elemental");
        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("Declining to sacrifice a land sacrifices Bog Elemental")
    void decliningSacrificesElemental() {
        harness.addToBattlefield(player1, new BogElemental());
        harness.addToBattlefield(player1, new Forest());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Bog Elemental");
        harness.assertInGraveyard(player1, "Bog Elemental");
        harness.assertOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("An opponent's land does not satisfy the requirement")
    void opponentLandDoesNotCount() {
        harness.addToBattlefield(player1, new BogElemental());
        harness.addToBattlefield(player2, new Island());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Bog Elemental");
        harness.assertInGraveyard(player1, "Bog Elemental");
        harness.assertOnBattlefield(player2, "Island");
    }
}
