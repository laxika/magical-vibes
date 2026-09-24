package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InfernalOffering.class, GrizzlyBears.class, Island.class})
class InfernalOfferingTest extends BaseCardTest {

    @Test
    void sacrificesCreaturesAndBothPlayersDrawTwo() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        harness.setLibrary(player2, List.of(new Island(), new Island()));
        castOffering(0);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(card -> card instanceof GrizzlyBears);
        assertThat(gd.playerGraveyards.get(player2.getId())).anyMatch(card -> card instanceof GrizzlyBears);
        assertThat(gd.playerHands.get(player1.getId())).filteredOn(card -> card instanceof Island).hasSize(2);
        assertThat(gd.playerHands.get(player2.getId())).filteredOn(card -> card instanceof Island).hasSize(2);
    }

    @Test
    void onlyPlayersWhoSacrificedDrawTwo() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        castOffering(0);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).filteredOn(card -> card instanceof Island).hasSize(2);
        assertThat(gd.playerHands.get(player2.getId())).filteredOn(card -> card instanceof Island).isEmpty();
    }

    @Test
    void returnsOneCreatureFromEachGraveyardInOrder() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));
        castOffering(1);

        PendingInteraction.GraveyardChoice firstChoice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(firstChoice).isNotNull();
        assertThat(firstChoice.playerId()).isEqualTo(player1.getId());
        harness.handleGraveyardCardChosen(player1, firstChoice.validIndices().getFirst());

        PendingInteraction.GraveyardChoice secondChoice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(secondChoice).isNotNull();
        assertThat(secondChoice.playerId()).isEqualTo(player2.getId());
        harness.handleGraveyardCardChosen(player2, secondChoice.validIndices().getFirst());

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(card -> card instanceof GrizzlyBears);
        assertThat(gd.playerGraveyards.get(player2.getId())).noneMatch(card -> card instanceof GrizzlyBears);
    }

    private void castOffering(int modeIndex) {
        harness.setHand(player1, List.of(new InfernalOffering()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castModalSorcery(player1, 0, modeIndex, List.of());
        harness.passBothPriorities();
    }
}
