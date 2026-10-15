package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.FakeConnection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RiddlesInTheDark.class, Island.class, Forest.class, Swamp.class, Plains.class})
class RiddlesInTheDarkTest extends BaseCardTest {

    @Test
    @DisplayName("The opponent can choose the face-down pile")
    void opponentChoosesFaceDownPile() {
        Card island = new Island();
        Card forest = new Forest();
        Card swamp = new Swamp();
        Card plains = new Plains();
        castRiddlesInTheDark(island, forest, swamp, plains);

        harness.handleMultipleCardsChosen(player1, List.of(island.getId(), forest.getId()));

        PendingInteraction.MayAbilityChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.description()).contains("Island", "Forest", "2 cards")
                .doesNotContain("Swamp", "Plains");

        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(swamp, plains);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(island, forest);
    }

    @Test
    @DisplayName("Choosing the face-up pile puts it into hand and the face-down pile into the graveyard")
    void opponentChoosesFaceUpPile() {
        Card island = new Island();
        Card forest = new Forest();
        Card swamp = new Swamp();
        Card plains = new Plains();
        castRiddlesInTheDark(island, forest, swamp, plains);

        harness.handleMultipleCardsChosen(player1, List.of(island.getId(), forest.getId()));
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(island, forest)
                .doesNotContain(swamp, plains);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(swamp, plains);
    }

    @Test
    void opponentCanChooseAnEmptyFaceUpPile() {
        Card island = new Island();
        Card forest = new Forest();
        castRiddlesInTheDark(island, forest);

        harness.handleMultipleCardsChosen(player1, List.of());
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(island, forest);
    }

    @Test
    void opponentCanChooseAnEmptyFaceDownPile() {
        Card island = new Island();
        Card forest = new Forest();
        castRiddlesInTheDark(island, forest);

        harness.handleMultipleCardsChosen(player1, List.of(island.getId(), forest.getId()));
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(island, forest);
    }

    @Test
    void opponentCanChooseTheEntireFaceDownPile() {
        Card island = new Island();
        Card forest = new Forest();
        Card swamp = new Swamp();
        Card plains = new Plains();
        castRiddlesInTheDark(island, forest, swamp, plains);

        harness.handleMultipleCardsChosen(player1, List.of());
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(island, forest, swamp, plains);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .hasSize(1).first().isInstanceOf(RiddlesInTheDark.class);
    }

    @Test
    void usesAllAvailableCardsWhenLibraryHasFewerThanFour() {
        Card island = new Island();
        Card forest = new Forest();
        castRiddlesInTheDark(island, forest);

        harness.handleMultipleCardsChosen(player1, List.of(island.getId()));
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(island).doesNotContain(forest);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    void leavesCardsBelowTheTopFourInLibrary() {
        Card island = new Island();
        Card forest = new Forest();
        Card swamp = new Swamp();
        Card plains = new Plains();
        Card fifth = new Island();
        castRiddlesInTheDark(island, forest, swamp, plains, fifth);

        harness.handleMultipleCardsChosen(player1, List.of(island.getId(), forest.getId()));
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(island, forest);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(swamp, plains).doesNotContain(fifth);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(fifth);
    }

    @Test
    void emptyLibraryDoesNotRequireAChoiceOrCauseADrawLoss() {
        castRiddlesInTheDark();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .hasSize(1).first().isInstanceOf(RiddlesInTheDark.class);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    void multiplayerStillRequiresSeparationAndAnOpponentsPileChoice() {
        Player third = new Player(UUID.randomUUID(), "Charlie");
        UUID thirdId = third.getId();
        gd.playerIds.add(thirdId);
        gd.orderedPlayerIds.add(thirdId);
        gd.playerNames.add("Charlie");
        gd.playerIdToName.put(thirdId, "Charlie");
        gd.playerDecks.put(thirdId, new ArrayList<>());
        gd.playerHands.put(thirdId, new ArrayList<>());
        gd.playerBattlefields.put(thirdId, new ArrayList<>());
        gd.playerGraveyards.put(thirdId, new ArrayList<>());
        gd.playerCommandZones.put(thirdId, new ArrayList<>());
        gd.playerManaPools.put(thirdId, new ManaPool());
        gd.playerLifeTotals.put(thirdId, 20);
        harness.getSessionManager().registerPlayer(new FakeConnection("conn-3"), thirdId, "Charlie");

        Card island = new Island();
        Card forest = new Forest();
        Card swamp = new Swamp();
        Card plains = new Plains();
        castRiddlesInTheDark(island, forest, swamp, plains);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        PendingInteraction.PermanentChoice opponentChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(opponentChoice).isNotNull();
        assertThat(opponentChoice.playerId()).isEqualTo(player1.getId());
        assertThat(opponentChoice.validPlayerIds()).containsExactlyInAnyOrder(player2.getId(), thirdId);
        harness.handlePermanentChosen(player1, thirdId);
        PendingInteraction.MultiGraveyardChoice separation =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(separation).isNotNull();
        assertThat(separation.playerId()).isEqualTo(player1.getId());
        harness.handleMultipleCardsChosen(player1, List.of(island.getId(), forest.getId()));
        PendingInteraction.MayAbilityChoice pileChoice =
                gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        assertThat(pileChoice).isNotNull();
        assertThat(pileChoice.playerId()).isEqualTo(thirdId);
        harness.handleMayAbilityChosen(third, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(swamp, plains);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(island, forest);
        assertThat(gd.playerHands.get(thirdId)).isEmpty();
    }

    private void castRiddlesInTheDark(Card... library) {
        harness.setLibrary(player1, List.of(library));
        harness.castFromHand(player1, new RiddlesInTheDark(), "{2}{U}");
        harness.passBothPriorities();
    }
}
