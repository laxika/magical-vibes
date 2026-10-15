package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.o.OutlawMedic;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({InsatiableAvarice.class, OutlawMedic.class, Swamp.class})
class InsatiableAvariceTest extends BaseCardTest {

    @Test
    @DisplayName("Search mode puts the chosen card on top of the library")
    void searchModePutsChosenCardOnTop() {
        Card first = new OutlawMedic();
        Card second = new Swamp();
        harness.setLibrary(player1, List.of(first, second));
        cast(new int[]{0}, List.of(), 3);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, first);
    }

    @Test
    @DisplayName("Draw and life-loss mode affects the chosen player")
    void drawAndLifeLossModeAffectsTarget() {
        Card first = new OutlawMedic();
        Card second = new Swamp();
        Card third = new OutlawMedic();
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
        Card first = new OutlawMedic();
        Card second = new Swamp();
        harness.setLibrary(player1, List.of(first, second));
        int handBefore = gd.playerHands.get(player2.getId()).size();

        cast(new int[]{0, 1}, List.of(player2.getId()), 5);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore + 3);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("Draw and life-loss mode cannot target a permanent")
    void drawAndLifeLossModeRejectsPermanentTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new OutlawMedic());

        assertThatThrownBy(() -> cast(new int[]{1}, List.of(creature.getId()), 3))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Both modes search before drawing when targeting the caster")
    void bothModesDrawSearchedCardForCaster() {
        Card chosen = new OutlawMedic();
        Card second = new Swamp();
        Card third = new Swamp();
        Card fourth = new Swamp();
        harness.setLibrary(player1, List.of(second, third, fourth, chosen));

        cast(new int[]{1, 0}, List.of(player1.getId()), 5);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        harness.handleCardChosen(player1, 3);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3).contains(chosen);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1).doesNotContain(chosen);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("Search mode accepts one black mana and two colorless mana")
    void searchModePaysGenericAdditionalCost() {
        Card chosen = new Swamp();
        harness.setLibrary(player1, List.of(chosen));
        harness.setHand(player1, List.of(new InsatiableAvarice()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castModalSorceryWithModes(player1, 0, 1, 2, new int[]{0}, List.of(), null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(chosen);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Draw mode cannot substitute colorless mana for its black additional cost")
    void drawModeRequiresThreeBlackMana() {
        harness.setHand(player1, List.of(new InsatiableAvarice()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castModalSorceryWithModes(
                player1, 0, 1, 2, new int[]{1}, List.of(player1.getId()), null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Searching an empty library does not prevent the second mode resolving")
    void emptySearchStillDrawsForOpponent() {
        harness.setLibrary(player1, List.of());
        Card first = new Swamp();
        Card second = new OutlawMedic();
        Card third = new Swamp();
        harness.setLibrary(player2, List.of(first, second, third));
        int handBefore = gd.playerHands.get(player2.getId()).size();

        cast(new int[]{0, 1}, List.of(player2.getId()), 5);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore + 3)
                .contains(first, second, third);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    private void cast(int[] modes, List<java.util.UUID> targets, int totalMana) {
        harness.setHand(player1, List.of(new InsatiableAvarice()));
        harness.addMana(player1, ManaColor.BLACK, Math.min(3, totalMana));
        harness.addMana(player1, ManaColor.COLORLESS, Math.max(0, totalMana - 3));
        harness.castModalSorceryWithModes(player1, 0, 1, 2, modes, targets, null);
        harness.passBothPriorities();
    }
}
