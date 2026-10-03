package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FoulImp;
import com.github.laxika.magicalvibes.cards.w.WallOfSouls;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BottomlessPit.class, FoulImp.class, WallOfSouls.class})
class BottomlessPitTest extends BaseCardTest {

    @Test
    @DisplayName("At the controller's upkeep, the controller discards a card at random")
    void controllerDiscardsAtOwnUpkeep() {
        harness.addToBattlefield(player1, new BottomlessPit());
        harness.setHand(player1, List.of(new FoulImp(), new WallOfSouls()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("discards") && log.contains("at random"));
    }

    @Test
    @DisplayName("At an opponent's upkeep, that opponent discards a card at random")
    void opponentDiscardsAtTheirUpkeep() {
        harness.addToBattlefield(player1, new BottomlessPit());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new FoulImp(), new WallOfSouls()));

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does nothing when the active player has no cards in hand")
    void emptyHandDiscardsNothing() {
        harness.addToBattlefield(player1, new BottomlessPit());
        harness.setHand(player1, List.of());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Each copy triggers separately and only the active player discards")
    void multipleCopiesDiscardOneCardEach() {
        harness.addToBattlefield(player1, new BottomlessPit());
        harness.addToBattlefield(player2, new BottomlessPit());
        FoulImp inactiveCard = new FoulImp();
        harness.setHand(player1, List.of(inactiveCard));
        FoulImp firstCard = new FoulImp();
        WallOfSouls secondCard = new WallOfSouls();
        harness.setHand(player2, List.of(firstCard, secondCard));

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .containsExactlyInAnyOrder(firstCard, secondCard);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(inactiveCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("An empty hand at trigger time does not prevent discarding at resolution")
    void usesHandAtResolution() {
        harness.addToBattlefield(player1, new BottomlessPit());
        harness.setHand(player2, List.of());

        advanceToUpkeep(player2);

        assertThat(gd.stack).hasSize(1);
        FoulImp drawnCard = new FoulImp();
        harness.setHand(player2, List.of(drawnCard));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(drawnCard);
    }
}
