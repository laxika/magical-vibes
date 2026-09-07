package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FalseMemories.class, Island.class})
class FalseMemoriesTest extends BaseCardTest {

    @Test
    @DisplayName("Mills seven cards and exiles seven cards at the next end step")
    void millsThenExilesSevenCardsAtNextEndStep() {
        harness.setHand(player1, List.of(new FalseMemories()));
        harness.setLibrary(player1, islands(7));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(8);
        assertThat(gd.exiledCards).isEmpty();

        resolveAtNextEndStep();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        for (int i = 0; i < 7; i++) {
            harness.handleGraveyardCardChosen(player1, 0);
        }

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(7);
    }

    @Test
    @DisplayName("Exiles all remaining graveyard cards when fewer than seven remain")
    void exilesFewerThanSevenRemainingCards() {
        harness.setHand(player1, List.of(new FalseMemories()));
        harness.setLibrary(player1, islands(3));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0);
        resolveAtNextEndStep();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(4);
    }

    private void resolveAtNextEndStep() {
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private List<Card> islands(int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new Island());
        }
        return cards;
    }
}
