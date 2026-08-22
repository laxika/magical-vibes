package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({InsatiableAvarice.class, GrizzlyBears.class, Swamp.class})
class InsatiableAvariceTest extends BaseCardTest {

    @Test
    @DisplayName("Search mode puts the chosen card on top of the library")
    void searchModePutsChosenCardOnTop() {
        Card first = new GrizzlyBears();
        Card second = new Swamp();
        harness.setLibrary(player1, List.of(first, second));

        cast(new int[]{0}, List.of(), 3);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.LibraryCardChosen(1));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, first);
    }

    @Test
    @DisplayName("Draw and life-loss mode affects the chosen player")
    void drawAndLifeLossModeAffectsTarget() {
        Card first = new GrizzlyBears();
        Card second = new Swamp();
        Card third = new GrizzlyBears();
        harness.setLibrary(player2, List.of(first, second, third));
        int handBefore = gd.playerHands.get(player2.getId()).size();

        cast(new int[]{1}, List.of(player2.getId()), 3);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore + 3);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Both modes resolve and charge both additional costs")
    void bothModesResolve() {
        Card first = new GrizzlyBears();
        Card second = new Swamp();
        harness.setLibrary(player1, List.of(first, second));

        cast(new int[]{0, 1}, List.of(player2.getId()), 5);
        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(3);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("Draw and life-loss mode cannot target a permanent")
    void drawAndLifeLossModeRejectsPermanentTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThatThrownBy(() -> cast(new int[]{1}, List.of(creature.getId()), 3))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(int[] modes, List<java.util.UUID> targets, int totalMana) {
        harness.setHand(player1, List.of(new InsatiableAvarice()));
        harness.addMana(player1, ManaColor.BLACK, Math.min(3, totalMana));
        harness.addMana(player1, ManaColor.COLORLESS, Math.max(0, totalMana - 3));
        harness.castModalSorceryWithModes(player1, 0, 1, 2, modes, targets, null);
        harness.passBothPriorities();
    }
}
