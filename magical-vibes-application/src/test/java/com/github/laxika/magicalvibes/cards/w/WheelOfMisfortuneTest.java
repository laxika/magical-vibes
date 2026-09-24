package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WheelOfMisfortune.class, Island.class, Mountain.class})
class WheelOfMisfortuneTest extends BaseCardTest {

    @Test
    void highestChooserTakesDamageAndOtherPlayersReplaceTheirHands() {
        List<Card> drawnCards = List.of(
                new Mountain(), new Mountain(), new Mountain(), new Mountain(),
                new Mountain(), new Mountain(), new Mountain());
        harness.setHand(player1, List.of(new WheelOfMisfortune(), new Mountain()));
        harness.setHand(player2, List.of(new Island(), new Island()));
        harness.setLibrary(player1, List.copyOf(drawnCards));
        harness.setLibrary(player2, List.of(new Island(), new Island()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.XValueChoice.class);

        harness.handleXValueChosen(player1, 5);
        harness.handleXValueChosen(player2, 3);

        harness.assertLife(player1, 15);
        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player1.getId())).containsExactlyElementsOf(drawnCards);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Mountain"));
    }

    @Test
    void tiedHighestPlayersTakeDamageAndTiedLowestPlayersDoNotDiscard() {
        harness.setHand(player1, List.of(new WheelOfMisfortune()));
        harness.setHand(player2, List.of(new Island()));
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 4);
        harness.handleXValueChosen(player2, 4);

        harness.assertLife(player1, 16);
        harness.assertLife(player2, 16);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }
}
