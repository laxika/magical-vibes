package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BondOfAgony.class})
class BondOfAgonyTest extends BaseCardTest {

    @Test
    @DisplayName("Pays X life and makes each opponent lose X life")
    void paysLifeAndMakesOpponentLoseLife() {
        harness.setLife(player1, 15);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new BondOfAgony()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castAndResolveSorcery(player1, 0, 5);

        harness.assertLife(player1, 10);
        harness.assertLife(player2, 15);
    }

    @Test
    @DisplayName("X=0 causes no life loss")
    void xZeroDoesNothing() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new BondOfAgony()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Life is paid when casting, while the opponent loses life only on resolution")
    void paysLifeBeforeResolution() {
        harness.setHand(player1, List.of(new BondOfAgony()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, 4);

        harness.assertLife(player1, 16);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertLife(player1, 16);
        harness.assertLife(player2, 16);
        harness.assertInGraveyard(player1, "Bond of Agony");
    }

    @Test
    @DisplayName("The second player pays the cost and only the other player loses life on resolution")
    void secondPlayerCanCast() {
        harness.forceActivePlayer(player2);
        harness.setLife(player1, 18);
        harness.setLife(player2, 12);
        harness.setHand(player2, List.of(new BondOfAgony()));
        harness.addMana(player2, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player2, 0, 3);

        harness.assertLife(player1, 15);
        harness.assertLife(player2, 9);
    }

    @Test
    @DisplayName("Cannot pay more life than the caster has")
    void cannotPayMoreLifeThanAvailable() {
        harness.setLife(player1, 3);
        harness.setHand(player1, List.of(new BondOfAgony()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 4))
                .isInstanceOf(IllegalStateException.class);
        harness.assertLife(player1, 3);
    }

    @Test
    @DisplayName("Paying all life ends the game before the spell resolves")
    void payingAllLifeEndsGameBeforeResolution() {
        harness.setLife(player1, 5);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new BondOfAgony()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castAndResolveSorcery(player1, 0, 5);

        harness.assertLife(player1, 0);
        harness.assertLife(player2, 20);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }
}
