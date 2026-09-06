package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({JandorsRing.class, Forest.class, GrizzlyBears.class})
class JandorsRingTest extends BaseCardTest {

    @Test
    @DisplayName("Discards the last card drawn this turn and draws a card")
    void discardsLastDrawnCardAndDraws() {
        harness.addToBattlefield(player1, new JandorsRing());
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Forest()));
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);

        PendingInteraction.DiscardCostChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.DiscardCostChoice.class);
        assertThat(choice.validIndices()).containsExactly(1);
        harness.handleCardChosen(player1, 1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).extracting(card -> card.getName())
                .containsExactly("Forest", "Forest");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot activate without a card drawn this turn")
    void cannotActivateWithoutDrawnCard() {
        harness.addToBattlefield(player1, new JandorsRing());
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Only the most recently drawn card can pay the discard cost")
    void onlyMostRecentlyDrawnCardCanPayCost() {
        Permanent ring = harness.addToBattlefieldAndReturn(player1, new JandorsRing());
        Forest cardAlreadyInHand = new Forest();
        GrizzlyBears firstDraw = new GrizzlyBears();
        Forest lastDraw = new Forest();
        Forest replacementDraw = new Forest();
        harness.setHand(player1, List.of(cardAlreadyInHand));
        harness.setLibrary(player1, List.of(firstDraw, lastDraw, replacementDraw));
        harness.inMutationScope(() -> {
            harness.getDrawService().resolveDrawCard(gd, player1.getId());
            harness.getDrawService().resolveDrawCard(gd, player1.getId());
        });
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);

        PendingInteraction.DiscardCostChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.DiscardCostChoice.class);
        assertThat(choice.validIndices()).containsExactly(2);
        harness.handleCardChosen(player1, 2);
        assertThat(ring.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactly(cardAlreadyInHand, firstDraw, replacementDraw);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(lastDraw);
    }

    @Test
    @DisplayName("Cannot use an earlier draw when the last draw is no longer in hand")
    void cannotUseEarlierDrawWhenLastDrawIsGone() {
        Permanent ring = harness.addToBattlefieldAndReturn(player1, new JandorsRing());
        Forest cardAlreadyInHand = new Forest();
        GrizzlyBears firstDraw = new GrizzlyBears();
        Forest lastDraw = new Forest();
        harness.setHand(player1, List.of(cardAlreadyInHand));
        harness.setLibrary(player1, List.of(firstDraw, lastDraw));
        harness.inMutationScope(() -> {
            harness.getDrawService().resolveDrawCard(gd, player1.getId());
            harness.getDrawService().resolveDrawCard(gd, player1.getId());
        });
        gd.playerHands.get(player1.getId()).remove(lastDraw);
        harness.setGraveyard(player1, List.of(lastDraw));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(ring.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(cardAlreadyInHand, firstDraw);
    }
}
