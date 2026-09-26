package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.w.WallOfHeat;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChainsOfMephistopheles.class, WallOfHeat.class})
class ChainsOfMephistophelesTest extends BaseCardTest {

    @Test
    void firstDrawOfDrawStepIsNotReplaced() {
        harness.addToBattlefield(player1, new ChainsOfMephistopheles());
        CardFixture fixture = new CardFixture();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(fixture.libraryCard));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        drawCard(player1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(fixture.libraryCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void secondDrawOfDrawStepIsReplaced() {
        harness.addToBattlefield(player1, new ChainsOfMephistopheles());
        CardFixture fixture = new CardFixture();
        WallOfHeat secondLibraryCard = new WallOfHeat();
        harness.setHand(player1, List.of(fixture.handCard));
        harness.setLibrary(player1, List.of(fixture.libraryCard, secondLibraryCard));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        drawCard(player1);
        drawCard(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(fixture.libraryCard, secondLibraryCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(fixture.handCard);
    }

    @Test
    void extraDrawPromptsForDiscardAndThenDraws() {
        harness.addToBattlefield(player1, new ChainsOfMephistopheles());
        CardFixture fixture = new CardFixture();
        harness.setHand(player1, List.of(fixture.handCard));
        harness.setLibrary(player1, List.of(fixture.libraryCard));

        drawOutsideDrawStep(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(fixture.libraryCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(fixture.handCard);
    }

    @Test
    void emptyHandMillsInsteadOfDrawing() {
        harness.addToBattlefield(player1, new ChainsOfMephistopheles());
        CardFixture fixture = new CardFixture();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(fixture.libraryCard));

        drawOutsideDrawStep(player1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(fixture.libraryCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void everyCardInAMultiCardDrawIsReplaced() {
        harness.addToBattlefield(player1, new ChainsOfMephistopheles());
        CardFixture fixture = new CardFixture();
        WallOfHeat secondHandCard = new WallOfHeat();
        WallOfHeat secondLibraryCard = new WallOfHeat();
        harness.setHand(player1, List.of(fixture.handCard, secondHandCard));
        harness.setLibrary(player1, List.of(fixture.libraryCard, secondLibraryCard));

        drawCardsOutsideDrawStep(player1, 2);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(fixture.libraryCard, secondLibraryCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(fixture.handCard, secondHandCard);
    }

    @Test
    void replacesDrawsByOpponents() {
        harness.addToBattlefield(player1, new ChainsOfMephistopheles());
        CardFixture fixture = new CardFixture();
        harness.setHand(player2, List.of(fixture.handCard));
        harness.setLibrary(player2, List.of(fixture.libraryCard));

        drawOutsideDrawStep(player2);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(fixture.libraryCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(fixture.handCard);
    }

    @Test
    void multipleChainsApplySequentially() {
        harness.addToBattlefield(player1, new ChainsOfMephistopheles());
        harness.addToBattlefield(player1, new ChainsOfMephistopheles());
        CardFixture fixture = new CardFixture();
        WallOfHeat secondHandCard = new WallOfHeat();
        harness.setHand(player1, List.of(fixture.handCard, secondHandCard));
        harness.setLibrary(player1, List.of(fixture.libraryCard));

        drawOutsideDrawStep(player1);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(fixture.libraryCard);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactly(fixture.handCard, secondHandCard);
    }

    private void drawOutsideDrawStep(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        drawCard(player);
    }

    private void drawCardsOutsideDrawStep(Player player, int count) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCards(gd, player.getId(), count));
    }

    private void drawCard(Player player) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player.getId()));
    }

    private static final class CardFixture {
        private final WallOfHeat handCard = new WallOfHeat();
        private final WallOfHeat libraryCard = new WallOfHeat();
    }
}
