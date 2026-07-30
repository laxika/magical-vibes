package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

class SphinxOfUthuunTest extends BaseCardTest {

    private void castSphinxAndReachSeparation(List<Card> library) {
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new SphinxOfUthuun()));
        // Sphinx of Uthuun costs {5}{U}{U}
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
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
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.hasPendingInteraction(PendingPileSeparation.class)).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5);
    }
}
