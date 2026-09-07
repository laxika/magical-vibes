package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChainsOfMephistopheles.class, Forest.class, GrizzlyBears.class})
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
    void multipleChainsApplySequentially() {
        harness.addToBattlefield(player1, new ChainsOfMephistopheles());
        harness.addToBattlefield(player1, new ChainsOfMephistopheles());
        CardFixture fixture = new CardFixture();
        Card secondHandCard = new GrizzlyBears();
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

    private void drawCard(Player player) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player.getId()));
    }

    private static final class CardFixture {
        private final GrizzlyBears handCard = new GrizzlyBears();
        private final Forest libraryCard = new Forest();
    }
}
