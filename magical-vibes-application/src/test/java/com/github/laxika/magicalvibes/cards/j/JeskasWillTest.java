package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JeskasWill.class, GrizzlyBears.class})
class JeskasWillTest extends BaseCardTest {

    @Test
    @DisplayName("Mana mode adds red mana equal to target opponent's hand size")
    void manaModeAddsManaForTargetOpponentHand() {
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        castJeskasWill(new int[]{0}, List.of(player2.getId()), 3);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
    }

    @Test
    @DisplayName("Exile mode exiles the top three cards with play permission")
    void exileModeGrantsPlayPermission() {
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        Card third = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second, third));
        castJeskasWill(new int[]{1}, List.of(), 3);

        for (Card card : List.of(first, second, third)) {
            assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
            assertThat(gd.exilePlayPermissions.get(card.getId())).isEqualTo(player1.getId());
            assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(card.getId());
            assertThat(gd.exilePlayWithoutPayingManaCost).doesNotContain(card.getId());
        }
    }

    @Test
    @DisplayName("A commander allows both modes")
    void commanderAllowsBothModes() {
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        gd.playerCommandZones.get(player1.getId()).add(new GrizzlyBears());
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        Card third = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second, third));
        castJeskasWill(new int[]{0, 1}, List.of(player2.getId()), 3);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(first, second, third);
    }

    @Test
    @DisplayName("Mana mode cannot target the controller")
    void manaModeRejectsControllerTarget() {
        harness.setHand(player1, List.of(new JeskasWill()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castModalSorceryWithModes(
                player1, 0, 1, 2, new int[]{0}, List.of(player1.getId()), List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    @Test
    @DisplayName("Both modes require controlling a commander")
    void bothModesRequireCommander() {
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new JeskasWill()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castModalSorceryWithModes(
                player1, 0, 1, 2, new int[]{0, 1}, List.of(player2.getId()), List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("additional modal modes are not available");
    }

    private void castJeskasWill(int[] modes, List<java.util.UUID> targets, int mana) {
        harness.setHand(player1, List.of(new JeskasWill()));
        harness.addMana(player1, ManaColor.RED, mana);
        harness.castModalSorceryWithModes(player1, 0, 1, 2, modes, targets, List.of());
        harness.passBothPriorities();
    }
}
