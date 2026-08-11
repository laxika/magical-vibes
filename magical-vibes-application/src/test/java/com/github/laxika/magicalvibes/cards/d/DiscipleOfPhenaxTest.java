package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BlackKnight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
                .hasMessageContaining("Target must be a player");
    }
}
