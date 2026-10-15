package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.e.Eyetwitch;
import com.github.laxika.magicalvibes.cards.h.HuntForSpecimens;
import com.github.laxika.magicalvibes.cards.l.LashOfMalice;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GoBlank.class, Eyetwitch.class, HuntForSpecimens.class, LashOfMalice.class})
class GoBlankTest extends BaseCardTest {

    @Test
    @DisplayName("Target player discards two cards, then their graveyard is exiled")
    void discardsTwoThenExilesTargetGraveyard() {
        harness.setHand(player2, List.of(new Eyetwitch(), new HuntForSpecimens(), new LashOfMalice()));
        harness.setGraveyard(player2, List.of(new LashOfMalice()));
        harness.setHand(player1, List.of(new GoBlank()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(card -> card.getName())
                .contains("Lash of Malice", "Eyetwitch", "Hunt for Specimens");
        harness.assertInGraveyard(player1, "Go Blank");
    }

    @Test
    @DisplayName("Exiles the target player's graveyard even when they have no cards to discard")
    void emptyHandStillExilesGraveyard() {
        harness.setHand(player2, List.of());
        harness.setGraveyard(player2, List.of(new LashOfMalice()));
        harness.setHand(player1, List.of(new GoBlank()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Lash of Malice");
    }

    @Test
    @DisplayName("A player with one card discards it before their graveyard is exiled")
    void oneCardHandDiscardsAsMuchAsPossible() {
        harness.setHand(player2, List.of(new Eyetwitch()));
        harness.setGraveyard(player2, List.of(new LashOfMalice()));
        harness.setHand(player1, List.of(new GoBlank()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(card -> card.getName())
                .containsExactlyInAnyOrder("Eyetwitch", "Lash of Malice");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Go Blank");
    }

    @Test
    @DisplayName("Can target its caster and does not exile itself while resolving")
    void selfTargetExilesOnlyItsControllersGraveyard() {
        harness.setHand(player1, List.of(new GoBlank(), new Eyetwitch(), new HuntForSpecimens()));
        harness.setGraveyard(player1, List.of(new LashOfMalice()));
        harness.setGraveyard(player2, List.of(new HuntForSpecimens()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, player1.getId());
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactlyInAnyOrder("Lash of Malice", "Eyetwitch", "Hunt for Specimens");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(card -> card.getName()).containsExactly("Go Blank");
        harness.assertInGraveyard(player2, "Hunt for Specimens");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }
}
