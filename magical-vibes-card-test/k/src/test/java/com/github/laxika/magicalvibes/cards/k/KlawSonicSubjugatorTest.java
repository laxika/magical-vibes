package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KlawSonicSubjugator.class, GrizzlyBears.class, HillGiant.class, Shock.class})
class KlawSonicSubjugatorTest extends BaseCardTest {

    @Test
    @DisplayName("Reveals one card with no creature cards in the controller's graveyard")
    void revealsOneWithEmptyCreatureGraveyard() {
        harness.setHand(player2, List.of(new GrizzlyBears(), new HillGiant()));
        castKlaw(player2.getId());

        PendingInteraction.RevealCardsDiscardChoice reveal = activeChoice();
        assertThat(reveal.revealStage()).isTrue();
        assertThat(reveal.remainingCount()).isEqualTo(1);

        harness.handleCardChosen(player2, 1);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player2, "Hill Giant");
        assertThat(gd.playerHands.get(player2.getId()))
                .singleElement()
                .matches(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Adds each creature card in the controller's graveyard to the reveal count")
    void countsCreatureCardsInControllerGraveyard() {
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new HillGiant(), new Shock()));
        harness.setHand(player2, List.of(
                new GrizzlyBears(), new HillGiant(), new GrizzlyBears(), new HillGiant()));
        castKlaw(player2.getId());

        PendingInteraction.RevealCardsDiscardChoice reveal = activeChoice();
        assertThat(reveal.revealStage()).isTrue();
        assertThat(reveal.remainingCount()).isEqualTo(3);

        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 1);
        harness.handleCardChosen(player2, 2);
        harness.handleCardChosen(player1, 2);

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("The enter-the-battlefield ability can target only a player")
    void cannotTargetPermanent() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new KlawSonicSubjugator()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, permanent.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("This spell can only target players");
    }

    @Test
    @DisplayName("Reveals a short hand in full and discards only the controller's choice")
    void revealsWholeShortHand() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new HillGiant()));
        harness.setHand(player2, List.of(new GrizzlyBears(), new Shock()));
        castKlaw(player2.getId());

        assertThat(activeChoice().revealStage()).isFalse();
        assertThat(activeChoice().decidingPlayerId()).isEqualTo(player1.getId());
        assertThat(activeChoice().revealedCardIds()).hasSize(2);
        harness.handleCardChosen(player1, 1);

        harness.assertInGraveyard(player2, "Shock");
        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("An empty target hand finishes without requiring a choice")
    void emptyHandRequiresNoChoice() {
        harness.setHand(player2, List.of());
        castKlaw(player2.getId());

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        harness.assertOnBattlefield(player1, "Klaw, Sonic Subjugator");
    }

    @Test
    @DisplayName("Creature cards in the target's graveyard do not increase the reveal count")
    void ignoresOpponentsGraveyard() {
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new HillGiant()));
        harness.setHand(player2, List.of(new GrizzlyBears(), new Shock()));
        castKlaw(player2.getId());

        assertThat(activeChoice().remainingCount()).isEqualTo(1);
        harness.handleCardChosen(player2, 1);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player2, "Shock");
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Klaw can target its controller and discard from their own hand")
    void canTargetController() {
        harness.setHand(player1, List.of(new KlawSonicSubjugator(), new GrizzlyBears(), new Shock()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castCreature(player1, 0, 0, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(activeChoice().remainingCount()).isEqualTo(1);
        harness.handleCardChosen(player1, 1);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Shock");
        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The creature count is evaluated when the triggered ability resolves")
    void countsGraveyardAtResolution() {
        harness.setHand(player2, List.of(new GrizzlyBears(), new HillGiant(), new Shock()));
        harness.setHand(player1, List.of(new KlawSonicSubjugator()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castCreature(player1, 0, 0, player2.getId());
        harness.passBothPriorities();
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.passBothPriorities();

        assertThat(activeChoice().remainingCount()).isEqualTo(2);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 2);
        harness.handleCardChosen(player1, 1);

        harness.assertInGraveyard(player2, "Shock");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        harness.assertInHand(player2, "Hill Giant");
    }

    private PendingInteraction.RevealCardsDiscardChoice activeChoice() {
        return gd.interaction.activeInteraction(PendingInteraction.RevealCardsDiscardChoice.class);
    }

    private void castKlaw(java.util.UUID targetPlayerId) {
        harness.setHand(player1, List.of(new KlawSonicSubjugator()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castCreature(player1, 0, 0, targetPlayerId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
