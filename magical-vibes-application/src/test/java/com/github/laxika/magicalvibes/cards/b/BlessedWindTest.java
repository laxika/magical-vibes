package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent bear = findPermanent(player2, "Grizzly Bears");
        prepareCard();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, bear.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castTargeting(com.github.laxika.magicalvibes.model.Player target) {
        prepareCard();
        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private void prepareCard() {
        harness.setHand(player1, List.of(new BlessedWind()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 7);
    }
}
