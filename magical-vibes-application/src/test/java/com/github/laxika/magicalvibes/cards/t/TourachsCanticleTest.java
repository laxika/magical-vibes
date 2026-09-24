package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
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

@CardUsed({TourachsCanticle.class, Forest.class, GrizzlyBears.class})
class TourachsCanticleTest extends BaseCardTest {

    @Test
    @DisplayName("The controller chooses one card, then the target discards another at random")
    void choosesThenRandomlyDiscards() {
        harness.setHand(player2, new ArrayList<>(List.of(
                new Forest(), new GrizzlyBears(), new GrizzlyBears())));
        harness.setHand(player1, List.of(new TourachsCanticle()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        PendingInteraction.RevealedHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.choosingPlayerId()).isEqualTo(player1.getId());
        assertThat(choice.validIndices()).containsExactly(0, 1, 2);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player2, "Forest");
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gameLogContains("at random")).isTrue();
    }

    @Test
    @DisplayName("An empty target hand produces no choices or discards")
    void emptyHandDoesNothing() {
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new TourachsCanticle()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Only an opponent can be targeted")
    void targetsOpponentOnly() {
        harness.setHand(player1, List.of(new TourachsCanticle()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
