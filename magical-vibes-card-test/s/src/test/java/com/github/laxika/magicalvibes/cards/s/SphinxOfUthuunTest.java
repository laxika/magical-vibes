package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PendingPileSeparation;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.FakeConnection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.ArrayList;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SphinxOfUthuun.class, Island.class, Forest.class, Swamp.class, Plains.class,
        Mountain.class, RuneclawBear.class})
class SphinxOfUthuunTest extends BaseCardTest {

    private void castSphinxAndReachSeparation(List<Card> library) {
        harness.setLibrary(player1, library);
        harness.castFromHand(player1, new SphinxOfUthuun(), "{5}{U}{U}");
        harness.passBothPriorities(); // resolve the Sphinx -> enters, reveal trigger on stack
        harness.passBothPriorities(); // resolve reveal trigger -> opponent separates
    }

    @Test
    @DisplayName("ETB reveals the top five and prompts an opponent to separate them")
    void enterRevealsFiveAndPromptsOpponent() {
        castSphinxAndReachSeparation(List.of(new Island(), new Forest(), new Swamp(), new Plains(), new Mountain()));

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.hasPendingInteraction(PendingPileSeparation.class)).isTrue();
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validCardIds()).hasSize(5);
    }

    @Test
    @DisplayName("Choosing Pile 1 puts it into hand and the other pile into the graveyard")
    void chosenPileToHandOtherToGraveyard() {
        Card island = new Island();
        Card forest = new Forest();
        Card swamp = new Swamp();
        Card plains = new Plains();
        Card mountain = new Mountain();
        castSphinxAndReachSeparation(List.of(island, forest, swamp, plains, mountain));

        harness.handleMultipleCardsChosen(player2, List.of(island.getId(), forest.getId()));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(island, forest);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(swamp, plains, mountain);
        assertThat(gd.hasPendingInteraction(PendingPileSeparation.class)).isFalse();
    }

    @Test
    @DisplayName("Declining takes the other pile to hand and bins Pile 1")
    void decliningTakesPileTwoToHand() {
        Card island = new Island();
        Card forest = new Forest();
        Card swamp = new Swamp();
        Card plains = new Plains();
        Card mountain = new Mountain();
        castSphinxAndReachSeparation(List.of(island, forest, swamp, plains, mountain));

        harness.handleMultipleCardsChosen(player2, List.of(island.getId(), forest.getId()));
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).contains(swamp, plains, mountain);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(island, forest);
    }

    @Test
    @DisplayName("An empty pile is allowed — everything goes to the graveyard when Pile 1 is chosen")
    void emptyPileOneSendsEverythingToGraveyard() {
        Card island = new Island();
        Card forest = new Forest();
        Card swamp = new Swamp();
        Card plains = new Plains();
        Card mountain = new Mountain();
        castSphinxAndReachSeparation(List.of(island, forest, swamp, plains, mountain));

        harness.handleMultipleCardsChosen(player2, List.of());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(island, forest, swamp, plains, mountain);
    }

    @Test
    @DisplayName("A smaller library reveals only the cards available")
    void smallerLibraryRevealsWhatIsThere() {
        castSphinxAndReachSeparation(List.of(new Island(), new Forest()));

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).hasSize(2);
    }

    @Test
    @DisplayName("Another creature entering does not trigger the reveal")
    void otherCreatureEnteringDoesNotTrigger() {
        harness.addToBattlefield(player1, new SphinxOfUthuun());
        harness.setLibrary(player1, List.of(new Island(), new Forest(), new Swamp(), new Plains(), new Mountain()));
        harness.castFromHand(player1, new RuneclawBear(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.hasPendingInteraction(PendingPileSeparation.class)).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5);
    }

    @Test
    @DisplayName("Revealing an empty library does not draw cards or remove the Sphinx")
    void emptyLibraryFinishesWithoutDrawing() {
        castSphinxAndReachSeparation(List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        harness.assertOnBattlefield(player1, "Sphinx of Uthuun");
    }

    @Test
    @DisplayName("Only the top five cards are separated; the remaining library keeps its order")
    void cardsBelowTopFiveRemainInOrder() {
        Card island = new Island();
        Card forest = new Forest();
        Card swamp = new Swamp();
        Card plains = new Plains();
        Card mountain = new Mountain();
        Card sixth = new Island();
        Card seventh = new Forest();
        castSphinxAndReachSeparation(List.of(island, forest, swamp, plains, mountain, sixth, seventh));

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(
                island.getId(), forest.getId(), swamp.getId(), plains.getId(), mountain.getId());
        harness.handleMultipleCardsChosen(player2, List.of(island.getId(), forest.getId()));
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(island, forest);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyInAnyOrder(swamp, plains, mountain);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(sixth, seventh);
    }

    @Test
    @DisplayName("With two opponents the controller must still have an opponent separate the cards")
    void multipleOpponentsStillRequirePileSeparation() {
        UUID id = UUID.randomUUID();
        Player player3 = new Player(id, "Charlie");
        gd.playerIds.add(id);
        gd.orderedPlayerIds.add(id);
        gd.playerNames.add("Charlie");
        gd.playerIdToName.put(id, "Charlie");
        gd.playerDecks.put(id, new ArrayList<>());
        gd.playerHands.put(id, new ArrayList<>());
        gd.playerBattlefields.put(id, new ArrayList<>());
        gd.playerGraveyards.put(id, new ArrayList<>());
        gd.playerCommandZones.put(id, new ArrayList<>());
        gd.playerManaPools.put(id, new ManaPool());
        gd.playerLifeTotals.put(id, 20);
        harness.getSessionManager().registerPlayer(new FakeConnection("conn-3"), id, "Charlie");
        harness.setLibrary(player1, List.of(new Island(), new Forest(), new Swamp(), new Plains(), new Mountain()));
        harness.enterBattlefieldAndReturn(player1, new SphinxOfUthuun());

        harness.passBothPriorities();
        if (!gd.stack.isEmpty() && !gd.interaction.isAwaitingInput()) {
            harness.passPriority(player3);
        }

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
    }
}
