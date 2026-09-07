package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SkullcapSnail.class, GrizzlyBears.class})
class SkullcapSnailTest extends BaseCardTest {

    @Test
    @DisplayName("ETB prompts the target opponent to choose a card from their hand")
    void etbPromptsTargetOpponentToChooseCard() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new GrizzlyBears())));

        castAndResolve();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.ExileFromHandChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId())
                .isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Target opponent exiles the card they choose")
    void targetOpponentExilesChosenCard() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new GrizzlyBears())));

        castAndResolve();
        harness.handleCardChosen(player2, 1);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player2.getId()).getFirst().getName())
                .isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target yourself")
    void cannotTargetYourself() {
        harness.setHand(player1, List.of(new SkullcapSnail()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    private void castAndResolve() {
        harness.setHand(player1, List.of(new SkullcapSnail()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
