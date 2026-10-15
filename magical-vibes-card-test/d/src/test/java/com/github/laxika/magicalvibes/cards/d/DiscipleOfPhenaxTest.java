package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BlackKnight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DiscipleOfPhenax.class, BlackKnight.class, GrizzlyBears.class, HillGiant.class, DarkBetrayal.class})
class DiscipleOfPhenaxTest extends BaseCardTest {

    private PendingInteraction.RevealCardsDiscardChoice activeChoice() {
        return gd.interaction.activeInteraction(PendingInteraction.RevealCardsDiscardChoice.class);
    }

    private void castDisciple() {
        harness.setHand(player1, List.of(new DiscipleOfPhenax()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castCreature(player1, 0, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("ETB reveals cards equal to black devotion and discards the controller's choice")
    void etbUsesBlackDevotion() {
        harness.setHand(player2, List.of(new GrizzlyBears(), new HillGiant(), new GrizzlyBears()));

        castDisciple();

        PendingInteraction.RevealCardsDiscardChoice reveal = activeChoice();
        assertThat(reveal.revealStage()).isTrue();
        assertThat(reveal.remainingCount()).isEqualTo(2);

        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 1);

        assertThat(activeChoice().revealStage()).isFalse();
        harness.handleCardChosen(player1, 1);

        harness.assertInGraveyard(player2, "Hill Giant");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Additional black devotion increases the number of revealed cards")
    void additionalBlackDevotionIncreasesRevealCount() {
        harness.addToBattlefield(player1, new BlackKnight());
        harness.setHand(player2, List.of(
                new GrizzlyBears(), new HillGiant(), new GrizzlyBears(), new HillGiant(), new GrizzlyBears()));

        castDisciple();

        assertThat(activeChoice().remainingCount()).isEqualTo(4);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 1);
        harness.handleCardChosen(player2, 2);
        harness.handleCardChosen(player2, 3);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(4);
    }

    @Test
    @DisplayName("The ETB ability cannot target a permanent")
    void cannotTargetPermanent() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DiscipleOfPhenax()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, permanent.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("This spell can only target players");
    }

    @Test
    void revealsEntireSmallHandAndDiscardsOneCard() {
        DiscipleOfPhenax card = new DiscipleOfPhenax();
        harness.setHand(player2, List.of(card));

        castDisciple();

        assertThat(activeChoice().revealStage()).isFalse();
        assertThat(activeChoice().revealedCardIds()).containsExactly(card.getId());
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(card);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void emptyHandDoesNotRequireAChoice() {
        harness.setHand(player2, List.of());

        castDisciple();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    void canTargetItsController() {
        DiscipleOfPhenax card = new DiscipleOfPhenax();
        harness.setHand(player1, List.of(new DiscipleOfPhenax(), card));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castCreature(player1, 0, 0, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(activeChoice().revealStage()).isFalse();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(card);
    }

    @Test
    void sourceLeavingBeforeResolutionReducesDevotionToZero() {
        DiscipleOfPhenax card = new DiscipleOfPhenax();
        harness.setHand(player2, List.of(new DarkBetrayal(), card));
        harness.setHand(player1, List.of(new DiscipleOfPhenax()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castCreature(player1, 0, 0, player2.getId());
        harness.passBothPriorities();

        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.castAndResolveInstant(player2, 0,
                harness.getPermanentId(player1, "Disciple of Phenax"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Disciple of Phenax");
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(card);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void discardChoiceUsesRevealedOrderAndIgnoresOpponentsDevotion() {
        DiscipleOfPhenax hidden = new DiscipleOfPhenax();
        DiscipleOfPhenax chosen = new DiscipleOfPhenax();
        DiscipleOfPhenax other = new DiscipleOfPhenax();
        harness.setHand(player2, List.of(hidden, chosen, other));
        harness.addToBattlefield(player2, new DiscipleOfPhenax());

        castDisciple();

        assertThat(activeChoice().remainingCount()).isEqualTo(2);
        harness.handleCardChosen(player2, 2);
        harness.handleCardChosen(player2, 1);
        assertThat(activeChoice().revealedCardIds()).containsExactly(other.getId(), chosen.getId());
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(chosen);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(hidden, other);
    }
}
