package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PendingPileSeparation;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SteamAuguryTest extends BaseCardTest {

    private void castSteamAugury(List<Card> library) {
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new SteamAugury()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("The controller separates the top five cards and the opponent chooses a pile")
    void controllerSeparatesOpponentChooses() {
        Card island = new Island();
        Card forest = new Forest();
        Card mountain = new Mountain();
        Card plains = new Plains();
        Card swamp = new Swamp();
        castSteamAugury(List.of(island, forest, mountain, plains, swamp));

        PendingInteraction.MultiGraveyardChoice separation =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(separation).isNotNull();
        assertThat(separation.playerId()).isEqualTo(player1.getId());
        assertThat(separation.validCardIds()).containsExactlyInAnyOrder(
                island.getId(), forest.getId(), mountain.getId(), plains.getId(), swamp.getId());

        harness.handleMultipleCardsChosen(player1, List.of(island.getId(), forest.getId()));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(island, forest);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(mountain, plains, swamp);
        assertThat(gd.hasPendingInteraction(PendingPileSeparation.class)).isFalse();
    }

    @Test
    @DisplayName("The opponent can choose the other pile")
    void opponentCanChoosePileTwo() {
        Card island = new Island();
        Card forest = new Forest();
        Card mountain = new Mountain();
        Card plains = new Plains();
        Card swamp = new Swamp();
        castSteamAugury(List.of(island, forest, mountain, plains, swamp));

        harness.handleMultipleCardsChosen(player1, List.of(island.getId(), forest.getId()));
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerHands.get(player1.getId())).contains(mountain, plains, swamp);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(island, forest);
    }
}
