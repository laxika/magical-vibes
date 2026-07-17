package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PendingPileSeparation;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BrilliantUltimatumTest extends BaseCardTest {

    private void addCastingMana() {
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.BLACK, 3);
    }

    @Test
    @DisplayName("Resolving exiles the top five cards and prompts the opponent to separate them")
    void resolutionExilesTopFiveAndPromptsOpponent() {
        Card forest = new Forest();
        Card bears = new GrizzlyBears();
        Card swamp = new Swamp();
        Card island = new Island();
        Card plains = new Plains();
        harness.setLibrary(player1, List.of(forest, bears, swamp, island, plains));
        harness.setHand(player1, List.of(new BrilliantUltimatum()));
        addCastingMana();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // All five cards are exiled from the library
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.findExiledCard(forest.getId())).isNotNull();
        assertThat(gd.findExiledCard(bears.getId())).isNotNull();

        // Opponent (player2) is prompted to separate them into piles
        assertThat(gd.hasPendingInteraction(PendingPileSeparation.class)).isTrue();
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validCardIds()).hasSize(5);
    }

    @Test
    @DisplayName("Chosen pile: the controller plays a land and casts a creature for free; the other pile stays exiled")
    void controllerPlaysLandAndCastsSpellFromChosenPile() {
        Card forest = new Forest();
        Card bears = new GrizzlyBears();
        Card swamp = new Swamp();
        Card island = new Island();
        Card plains = new Plains();
        harness.setLibrary(player1, List.of(forest, bears, swamp, island, plains));
        harness.setHand(player1, List.of(new BrilliantUltimatum()));
        addCastingMana();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // Opponent puts forest + bears in Pile 1
        harness.handleMultipleCardsChosen(player2, List.of(forest.getId(), bears.getId()));

        // Controller chooses Pile 1
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        // Controller is offered the pile's cards to play
        PendingInteraction.BrilliantUltimatumPlayChoice playChoice =
                gd.interaction.activeInteraction(PendingInteraction.BrilliantUltimatumPlayChoice.class);
        assertThat(playChoice).isNotNull();
        assertThat(playChoice.validCardIds()).containsExactlyInAnyOrder(forest.getId(), bears.getId());

        // Controller plays both
        harness.handleMultipleCardsChosen(player1, List.of(forest.getId(), bears.getId()));

        // The land is on the battlefield and used the land play
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(forest.getId()));
        assertThat(gd.landsPlayedThisTurn.getOrDefault(player1.getId(), 0)).isEqualTo(1);

        // The creature was cast for free — it is on the stack; resolve it
        assertThat(gd.stack).anyMatch(e -> e.getCard().getId().equals(bears.getId()));
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(bears.getId()));

        // The unchosen pile (swamp, island, plains) remains exiled
        assertThat(gd.findExiledCard(swamp.getId())).isNotNull();
        assertThat(gd.findExiledCard(island.getId())).isNotNull();
        assertThat(gd.findExiledCard(plains.getId())).isNotNull();

        assertThat(gd.hasPendingInteraction(PendingPileSeparation.class)).isFalse();
    }

    @Test
    @DisplayName("Declining the pile choice plays from the other pile")
    void controllerChoosesPileTwo() {
        Card bears = new GrizzlyBears();
        Card elves = new LlanowarElves();
        Card swamp = new Swamp();
        Card island = new Island();
        Card plains = new Plains();
        harness.setLibrary(player1, List.of(bears, elves, swamp, island, plains));
        harness.setHand(player1, List.of(new BrilliantUltimatum()));
        addCastingMana();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // Opponent puts bears in Pile 1, everything else in Pile 2
        harness.handleMultipleCardsChosen(player2, List.of(bears.getId()));

        // Controller chooses Pile 2 (elves + the lands)
        harness.handleMayAbilityChosen(player1, false);

        PendingInteraction.BrilliantUltimatumPlayChoice playChoice =
                gd.interaction.activeInteraction(PendingInteraction.BrilliantUltimatumPlayChoice.class);
        assertThat(playChoice.validCardIds())
                .containsExactlyInAnyOrder(elves.getId(), swamp.getId(), island.getId(), plains.getId());

        // Cast elves only from the chosen pile
        harness.handleMultipleCardsChosen(player1, List.of(elves.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(elves.getId()));
        // Bears was in the pile the controller did not choose — it stays exiled
        assertThat(gd.findExiledCard(bears.getId())).isNotNull();
    }

    @Test
    @DisplayName("A land can't be played if one was already played this turn; it stays exiled")
    void secondLandCannotBePlayed() {
        Card forest = new Forest();
        Card swamp = new Swamp();
        Card bears = new GrizzlyBears();
        Card island = new Island();
        Card plains = new Plains();
        harness.setLibrary(player1, List.of(forest, swamp, bears, island, plains));
        harness.setHand(player1, List.of(new BrilliantUltimatum()));
        addCastingMana();
        // Controller has already played their land for the turn
        gd.landsPlayedThisTurn.put(player1.getId(), 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.handleMultipleCardsChosen(player2, List.of(forest.getId()));
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultipleCardsChosen(player1, List.of(forest.getId()));

        // The land could not be played (limit reached) — it stays exiled, not on the battlefield
        assertThat(gd.findExiledCard(forest.getId())).isNotNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getId().equals(forest.getId()));
        assertThat(gd.landsPlayedThisTurn.getOrDefault(player1.getId(), 0)).isEqualTo(1);
    }
}
