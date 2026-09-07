package com.github.laxika.magicalvibes.cards.r;

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

@CardUsed({RaphaelsTechnique.class, GrizzlyBears.class})
class RaphaelsTechniqueTest extends BaseCardTest {

    @Test
    @DisplayName("Each player independently may discard their hand and draw seven cards")
    void playersChooseIndependently() {
        Card player1HandCard = new GrizzlyBears();
        Card player2HandCard = new GrizzlyBears();
        harness.setHand(player1, List.of(new RaphaelsTechnique(), player1HandCard));
        harness.setHand(player2, List.of(player2HandCard));
        fillLibrary(player1, 10);
        fillLibrary(player2, 10);

        castTechnique();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(player1HandCard);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(7);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(player2HandCard);
    }

    @Test
    @DisplayName("All choices happen before an accepted player's hand changes")
    void choicesCompleteBeforeResolution() {
        Card player1HandCard = new GrizzlyBears();
        Card player2HandCard = new GrizzlyBears();
        harness.setHand(player1, List.of(new RaphaelsTechnique(), player1HandCard));
        harness.setHand(player2, List.of(player2HandCard));
        fillLibrary(player1, 10);
        fillLibrary(player2, 10);

        castTechnique();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(player1HandCard);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(10);

        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(7);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(player2HandCard);
    }

    private void castTechnique() {
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castInstant(player1, 0);
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
