package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Peek;
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

@CardUsed({Monomania.class, GrizzlyBears.class, Peek.class, Forest.class})
class MonomaniaTest extends BaseCardTest {

    private void castMonomania(com.github.laxika.magicalvibes.model.Player targetPlayer) {
        harness.setHand(player1, List.of(new Monomania()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castAndResolveSorcery(player1, 0, targetPlayer.getId());
    }

    @Test
    @DisplayName("Target keeps one card of their choice and discards the rest")
    void targetKeepsOneCard() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Peek(), new Forest())));
        castMonomania(player2);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount()).isEqualTo(2);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId()).isEqualTo(player2.getId());

        harness.handleCardChosen(player2, 1); // discard Peek
        harness.handleCardChosen(player2, 1); // discard Forest

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId()).getFirst().getName()).isEqualTo("Grizzly Bears");
        harness.assertInGraveyard(player2, "Peek");
        harness.assertInGraveyard(player2, "Forest");
        harness.assertInGraveyard(player1, "Monomania");
    }

    @Test
    @DisplayName("Target with a single card discards nothing")
    void singleCardHandIsUntouched() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        castMonomania(player2);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Target with an empty hand discards nothing")
    void emptyHandIsUntouched() {
        harness.setHand(player2, new ArrayList<>(List.of()));
        castMonomania(player2);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Caster cannot make the discard choice for the target")
    void casterCannotChooseForTarget() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Peek())));
        castMonomania(player2);

        assertThatThrownBy(() -> harness.handleCardChosen(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not your turn to choose");
    }

    @Test
    @DisplayName("Can target yourself, keeping one card")
    void canTargetSelf() {
        harness.setHand(player1, new ArrayList<>(List.of(new Monomania(), new GrizzlyBears(), new Peek(), new Forest())));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castAndResolveSorcery(player1, 0, player1.getId());

        // Monomania has left the hand, so three cards remain and two must be discarded.
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount()).isEqualTo(2);

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst().getName()).isEqualTo("Forest");
    }

    @Test
    @DisplayName("Discard count uses the target's hand at resolution")
    void countsHandAtResolution() {
        harness.setHand(player1, List.of(new Monomania()));
        harness.setHand(player2, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castSorcery(player1, 0, player2.getId());

        Forest keptCard = new Forest();
        harness.setHand(player2, List.of(new Forest(), keptCard, new Forest(), new Forest()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount())
                .isEqualTo(3);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 1);
        harness.handleCardChosen(player2, 1);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(keptCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Targeting yourself with Monomania as your only card leaves an empty hand")
    void canTargetSelfWithNoCardsRemaining() {
        castMonomania(player1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Monomania");
    }
}
