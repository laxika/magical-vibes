package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Unhinge.class, GrizzlyBears.class})
class UnhingeTest extends BaseCardTest {

    @Test
    @DisplayName("Target player discards a card and the controller draws a card")
    void targetPlayerDiscardsAndControllerDraws() {
        Card discarded = new GrizzlyBears();
        Card drawn = new GrizzlyBears();
        harness.setHand(player1, List.of(new Unhinge()));
        harness.setHand(player2, List.of(discarded));
        harness.setLibrary(player1, List.of(drawn));
        addUnhingeMana();

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(discarded);
    }

    @Test
    @DisplayName("Can target its controller")
    void canTargetController() {
        Card discarded = new GrizzlyBears();
        Card remaining = new GrizzlyBears();
        Card drawn = new GrizzlyBears();
        Card spell = new Unhinge();
        harness.setHand(player1, List.of(spell, discarded, remaining));
        harness.setLibrary(player1, List.of(drawn));
        addUnhingeMana();

        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(drawn, remaining);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyInAnyOrder(discarded, spell);
    }

    @Test
    @DisplayName("Cannot target a permanent")
    void cannotTargetPermanent() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Unhinge()));
        addUnhingeMana();

        assertThatThrownBy(() -> harness.castSorcery(
                player1, 0, harness.getPermanentId(player2, "Grizzly Bears")))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addUnhingeMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
