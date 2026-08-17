package com.github.laxika.magicalvibes.cards.u;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UnderrealmLichTest extends BaseCardTest {

    private void drawWithLich() {
        harness.addToBattlefield(player1, new UnderrealmLich());
        harness.setHand(player1, new ArrayList<>());
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
    }

    @Test
    @DisplayName("A replaced draw puts the chosen card into hand and the rest into the graveyard")
    void chosenCardGoesToHandAndRestToGraveyard() {
        Card plains = new Plains();
        Card bears = new GrizzlyBears();
        Card forest = new Forest();
        Card island = new Island();
        harness.setLibrary(player1, new ArrayList<>(List.of(plains, bears, forest, island)));

        drawWithLich();

        PendingInteraction.LibraryRevealChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.allCards()).hasSize(3);
        assertThat(choice.remainingToGraveyard()).isTrue();

        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(bears);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(plains, forest);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(island);
    }

    @Test
    @DisplayName("With fewer than three cards, all available cards are used")
    void usesAvailableCardsOnly() {
        Card bears = new GrizzlyBears();
        Card plains = new Plains();
        harness.setLibrary(player1, new ArrayList<>(List.of(bears, plains)));

        drawWithLich();
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(bears);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(plains);
    }

    @Test
    @DisplayName("An empty library does not cause a loss when the draw is replaced")
    void emptyLibraryDoesNotLose() {
        harness.setLibrary(player1, new ArrayList<>());

        drawWithLich();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("The activated ability pays life, taps the Lich, and grants indestructible")
    void abilityPaysLifeTapsAndGrantsIndestructible() {
        Permanent lich = harness.addToBattlefieldAndReturn(player1, new UnderrealmLich());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(16);
        harness.passBothPriorities();

        assertThat(lich.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, lich, Keyword.INDESTRUCTIBLE)).isTrue();
    }
}
