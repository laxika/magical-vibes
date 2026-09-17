package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.s.ScornfulEgotist;
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

@CardUsed({Unburden.class, ScornfulEgotist.class})
class UnburdenTest extends BaseCardTest {

    private void giveUnburden() {
        harness.setHand(player1, List.of(new Unburden()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    @Test
    @DisplayName("Target player discards two cards of their choice")
    void targetDiscardsTwoChosenCards() {
        harness.setHand(player2, new ArrayList<>(List.of(
                new ScornfulEgotist(), new ScornfulEgotist(), new ScornfulEgotist())));
        giveUnburden();

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        // The targeted player (not the caster) chooses which two cards to discard.
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId()).isEqualTo(player2.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount()).isEqualTo(2);

        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Target with fewer than two cards discards their whole hand")
    void targetWithOneCardDiscardsWholeHand() {
        harness.setHand(player2, new ArrayList<>(List.of(new ScornfulEgotist())));
        giveUnburden();

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Empty-handed target discards nothing")
    void emptyHandDiscardsNothing() {
        harness.setHand(player2, new ArrayList<>());
        giveUnburden();

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Can target itself")
    void canTargetItself() {
        harness.setHand(player1, new ArrayList<>(List.of(
                new Unburden(), new ScornfulEgotist(), new ScornfulEgotist(), new ScornfulEgotist())));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId())
                .isEqualTo(player1.getId());
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Cycling discards the card and draws one")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new Unburden()));
        harness.setLibrary(player1, List.of(new ScornfulEgotist()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Unburden");
        harness.assertInHand(player1, "Scornful Egotist");
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new ScornfulEgotist());
        giveUnburden();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                harness.getPermanentId(player2, "Scornful Egotist")))
                .isInstanceOf(IllegalStateException.class);
    }
}
