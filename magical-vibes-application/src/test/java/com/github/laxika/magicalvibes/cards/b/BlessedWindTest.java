package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.p.PygmyRazorback;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BlessedWind.class, PygmyRazorback.class})
class BlessedWindTest extends BaseCardTest {

    @Test
    @DisplayName("Sets target opponent's life total to 20")
    void setsTargetOpponentsLifeTotalToTwenty() {
        harness.setLife(player2, 7);
        castTargeting(player2);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Can target the controller")
    void canTargetController() {
        harness.setLife(player1, 35);
        castTargeting(player1);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Cannot target a permanent")
    void cannotTargetPermanent() {
        harness.addToBattlefield(player2, new PygmyRazorback());
        Permanent boar = findPermanent(player2, "Pygmy Razorback");
        prepareCard();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, boar.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not change the other player's life total")
    void doesNotChangeOtherPlayersLifeTotal() {
        harness.setLife(player1, 11);
        harness.setLife(player2, 7);
        castTargeting(player2);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(11);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    private void castTargeting(Player target) {
        prepareCard();
        harness.castAndResolveSorcery(player1, 0, target.getId());
    }

    private void prepareCard() {
        harness.setHand(player1, List.of(new BlessedWind()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 7);
    }
}
