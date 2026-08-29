package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RixMaadiRevelerTest extends BaseCardTest {

    @Test
    @DisplayName("Normally discards a card, then draws a card when it enters")
    void normallyDiscardsThenDraws() {
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        harness.setHand(player1, new ArrayList<>(List.of(
                new RixMaadiReveler(), new GrizzlyBears(), new Shock())));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(card -> card.getName())
                .containsExactly("Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).extracting(card -> card.getName())
                .containsExactly("Shock", "Island");
    }

    @Test
    @DisplayName("For spectacle, discards the hand and draws three cards")
    void spectacleDiscardsHandThenDrawsThree() {
        gd.lifeLostThisTurn.put(player2.getId(), 1);
        harness.setLibrary(player1, List.of(new Island(), new Island(), new Island()));
        harness.setHand(player1, new ArrayList<>(List.of(
                new RixMaadiReveler(), new GrizzlyBears(), new Shock())));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).extracting(card -> card.getName())
                .containsExactly("Island", "Island", "Island");
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(card -> card.getName())
                .containsExactlyInAnyOrder("Grizzly Bears", "Shock");
    }

    @Test
    @DisplayName("Spectacle cannot be used unless an opponent lost life this turn")
    void spectacleRequiresOpponentLifeLoss() {
        harness.setHand(player1, List.of(new RixMaadiReveler()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }
}
