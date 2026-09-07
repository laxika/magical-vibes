package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TurtlesInTime.class, GrizzlyBears.class})
class TurtlesInTimeTest extends BaseCardTest {

    @Test
    @DisplayName("Returns all creatures, then players choose independently whether to shuffle and draw seven")
    void returnsCreaturesAndPlayersChooseIndependently() {
        Card player1Creature = new GrizzlyBears();
        Card player2Creature = new GrizzlyBears();
        Card player1HandCard = new GrizzlyBears();
        Card player2HandCard = new GrizzlyBears();
        harness.addToBattlefield(player1, player1Creature);
        harness.addToBattlefield(player2, player2Creature);
        harness.setHand(player1, List.of(new TurtlesInTime(), player1HandCard));
        harness.setHand(player2, List.of(player2HandCard));
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));
        fillLibrary(player1, 10);
        fillLibrary(player2, 10);

        castTurtlesInTime();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).contains(player1Creature);
        assertThat(gd.playerHands.get(player2.getId())).contains(player2Creature);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(player1HandCard, player1Creature);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(7);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Turtles in Time"));
    }

    @Test
    @DisplayName("Accepted players shuffle both zones before drawing seven cards")
    void acceptedPlayersShuffleHandAndGraveyard() {
        Card handCard = new GrizzlyBears();
        harness.setHand(player1, List.of(new TurtlesInTime(), handCard));
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setHand(player2, List.of());
        harness.setGraveyard(player2, List.of());
        fillLibrary(player1, 10);
        fillLibrary(player2, 10);

        castTurtlesInTime();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(7);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5);
    }

    private void castTurtlesInTime() {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    private void fillLibrary(Player player, int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new GrizzlyBears());
        }
        harness.setLibrary(player, cards);
    }
}
