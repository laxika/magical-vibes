package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.a.AvenTrooper;
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

@CardUsed({Unhinge.class, AvenTrooper.class})
class UnhingeTest extends BaseCardTest {

    @Test
    @DisplayName("Target player discards a card and the controller draws a card")
    void targetPlayerDiscardsAndControllerDraws() {
        Card discarded = new AvenTrooper();
        Card drawn = new AvenTrooper();
        harness.setHand(player1, List.of(new Unhinge()));
        harness.setHand(player2, List.of(discarded));
        harness.setLibrary(player1, List.of(drawn));
        addUnhingeMana();

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(discarded);
    }

    @Test
    @DisplayName("Can target its controller")
    void canTargetController() {
        Card discarded = new AvenTrooper();
        Card remaining = new AvenTrooper();
        Card drawn = new AvenTrooper();
        Card spell = new Unhinge();
        harness.setHand(player1, List.of(spell, discarded, remaining));
        harness.setLibrary(player1, List.of(drawn));
        addUnhingeMana();

        harness.castAndResolveSorcery(player1, 0, player1.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(drawn, remaining);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyInAnyOrder(discarded, spell);
    }

    @Test
    @DisplayName("Still draws when the target player has no cards to discard")
    void drawsWhenTargetHasEmptyHand() {
        Card drawn = new AvenTrooper();
        harness.setHand(player1, List.of(new Unhinge()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(drawn));
        addUnhingeMana();

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a permanent")
    void cannotTargetPermanent() {
        harness.addToBattlefield(player2, new AvenTrooper());
        harness.setHand(player1, List.of(new Unhinge()));
        addUnhingeMana();

        assertThatThrownBy(() -> harness.castSorcery(
                player1, 0, harness.getPermanentId(player2, "Aven Trooper")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Target player chooses the discarded card before the controller draws")
    void targetChoosesDiscardBeforeDraw() {
        Card kept = new AvenTrooper();
        Card discarded = new AvenTrooper();
        Card drawn = new AvenTrooper();
        harness.setHand(player1, List.of(new Unhinge()));
        harness.setHand(player2, List.of(kept, discarded));
        harness.setLibrary(player1, List.of(drawn));
        addUnhingeMana();

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawn);
        harness.handleCardChosen(player2, 1);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(kept);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(discarded);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    @DisplayName("Targeting self with no cards left after casting still draws")
    void selfTargetWithEmptyHandStillDraws() {
        Card spell = new Unhinge();
        Card drawn = new AvenTrooper();
        harness.setHand(player1, List.of(spell));
        harness.setLibrary(player1, List.of(drawn));
        addUnhingeMana();

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spell);
    }
    private void addUnhingeMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
