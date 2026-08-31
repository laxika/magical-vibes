package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WordsOfWaste.class, GrizzlyBears.class})
class WordsOfWasteTest extends BaseCardTest {

    @Test
    @DisplayName("The next draw is replaced by each opponent discarding a card")
    void replacesNextDrawWithOpponentDiscard() {
        harness.addToBattlefield(player1, new WordsOfWaste());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        activateWordsOfWaste();

        draw(player1);

        PendingInteraction.DiscardChoice discard =
                gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class);
        assertThat(discard.playerId()).isEqualTo(player2.getId());
        assertThat(discard.remainingCount()).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);

        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Repeated activations replace successive draws")
    void repeatedActivationsReplaceSuccessiveDraws() {
        harness.addToBattlefield(player1, new WordsOfWaste());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        activateWordsOfWaste();
        activateWordsOfWaste();

        drawAndDiscard(player1, player2);
        drawAndDiscard(player1, player2);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("A later draw is normal after the replacement is used")
    void laterDrawIsNormal() {
        harness.addToBattlefield(player1, new WordsOfWaste());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        activateWordsOfWaste();

        drawAndDiscard(player1, player2);
        draw(player1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    private void activateWordsOfWaste() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
    }

    private void drawAndDiscard(Player drawer, Player discarder) {
        draw(drawer);
        harness.handleCardChosen(discarder, 0);
    }

    private void draw(Player player) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player.getId()));
    }
}
