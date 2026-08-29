package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DrillBitTest extends BaseCardTest {

    @Test
    @DisplayName("Reveals the target hand and allows choosing a nonland card to discard")
    void choosesNonlandCardToDiscard() {
        harness.setHand(player2, new ArrayList<>(List.of(new Forest(), new GrizzlyBears(), new Peek())));
        harness.setHand(player1, List.of(new DrillBit()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        PendingInteraction.RevealedHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIndices()).containsExactly(1, 2);

        harness.handleCardChosen(player1, 1);

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId())).extracting(Card::getName)
                .containsExactly("Forest", "Peek");
    }

    @Test
    @DisplayName("A hand containing only lands has no legal discard choice")
    void onlyLandsCannotBeDiscarded() {
        harness.setHand(player2, List.of(new Forest()));
        harness.setHand(player1, List.of(new DrillBit()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).extracting(Card::getName)
                .containsExactly("Forest");
    }

    @Test
    @DisplayName("Spectacle casts Drill Bit for {B} after an opponent loses life")
    void spectacleUsesAlternateCost() {
        gd.lifeLostThisTurn.put(player2.getId(), 1);
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new DrillBit()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castWithAlternateCost(player1, 0, player2.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Spectacle is unavailable when no opponent has lost life")
    void spectacleRequiresOpponentLifeLoss() {
        harness.setHand(player1, List.of(new DrillBit()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
