package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MakeYourOwnLuck.class, GrizzlyBears.class, Forest.class})
class MakeYourOwnLuckTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles and plots a selected nonland card, putting the rest into hand")
    void plotsSelectedNonlandCardAndPutsRestIntoHand() {
        GrizzlyBears bears = new GrizzlyBears();
        Forest firstForest = new Forest();
        Forest secondForest = new Forest();
        harness.setHand(player1, List.of(new MakeYourOwnLuck()));
        harness.setLibrary(player1, List.of(firstForest, bears, secondForest));
        addMana();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 1);

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(bears);
        assertThat(gd.plottedCardIds).contains(bears.getId());
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstForest, secondForest);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Declining leaves all looked-at cards in hand")
    void decliningPutsAllCardsIntoHand() {
        GrizzlyBears bears = new GrizzlyBears();
        Forest firstForest = new Forest();
        Forest secondForest = new Forest();
        harness.setHand(player1, List.of(new MakeYourOwnLuck()));
        harness.setLibrary(player1, List.of(bears, firstForest, secondForest));
        addMana();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.plottedCardIds).doesNotContain(bears.getId());
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(bears, firstForest, secondForest);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("With no nonland card, puts all looked-at cards into hand without a prompt")
    void noNonlandCardNeedsNoChoice() {
        Forest firstForest = new Forest();
        Forest secondForest = new Forest();
        Forest thirdForest = new Forest();
        harness.setHand(player1, List.of(new MakeYourOwnLuck()));
        harness.setLibrary(player1, List.of(firstForest, secondForest, thirdForest));
        addMana();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstForest, secondForest, thirdForest);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
