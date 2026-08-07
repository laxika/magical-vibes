package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RogueElephantTest extends BaseCardTest {

    @Test
    @DisplayName("Auto-sacrifices when its controller has no Forest")
    void autoSacrificesWithoutForest() {
        harness.addToBattlefield(player1, new Island());

        castElephant();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Rogue Elephant");
        harness.assertInGraveyard(player1, "Rogue Elephant");
        harness.assertOnBattlefield(player1, "Island");
    }

    @Test
    @DisplayName("An opponent's Forest doesn't satisfy the requirement")
    void opponentForestDoesNotCount() {
        harness.addToBattlefield(player2, new Forest());

        castElephant();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Rogue Elephant");
        harness.assertOnBattlefield(player2, "Forest");
    }

    @Test
    @DisplayName("Sacrificing a Forest keeps Rogue Elephant on the battlefield")
    void sacrificingForestKeepsElephant() {
        harness.addToBattlefield(player1, new Forest());

        castElephant();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        UUID forestId = findPermanent(player1, "Forest").getId();
        harness.handlePermanentChosen(player1, forestId);

        harness.assertOnBattlefield(player1, "Rogue Elephant");
        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("Declining to sacrifice a Forest sacrifices Rogue Elephant")
    void decliningSacrificesElephant() {
        harness.addToBattlefield(player1, new Forest());

        castElephant();

        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Rogue Elephant");
        harness.assertInGraveyard(player1, "Rogue Elephant");
        harness.assertOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("Only Forests are offered as the permanent to sacrifice")
    void onlyForestsAreValidChoices() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());

        castElephant();

        harness.handleMayAbilityChosen(player1, true);

        UUID islandId = findPermanent(player1, "Island").getId();
        UUID forestId = findPermanent(player1, "Forest").getId();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validPermanentIds())
                .contains(forestId)
                .doesNotContain(islandId);
    }

    private void castElephant() {
        harness.setHand(player1, List.of(new RogueElephant()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell → ETB on stack
        harness.passBothPriorities(); // resolve ETB
    }
}
