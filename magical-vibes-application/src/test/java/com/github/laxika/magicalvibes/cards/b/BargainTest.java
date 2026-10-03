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

@CardUsed(Bargain.class)
class BargainTest extends BaseCardTest {

    @Test
    @DisplayName("Target opponent draws a card and the caster gains 7 life")
    void opponentDrawsCasterGainsLife() {
        harness.setHand(player1, List.of(new Bargain()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.setLife(player1, 20);
        int opponentHandBefore = gd.playerHands.get(player2.getId()).size();

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandBefore + 1);
        harness.assertLife(player1, 27);
    }

    @Test
    @DisplayName("Caster gains life before an opponent's empty-library loss is checked")
    void casterGainsLifeBeforeOpponentLosesFromEmptyLibrary() {
        harness.setHand(player1, List.of(new Bargain()));
        harness.setLibrary(player2, List.of());
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.setLife(player1, 20);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.assertLife(player1, 27);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("The other player can cast Bargain and gains life while its opponent draws")
    void otherPlayerCastsBargain() {
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Bargain()));
        harness.setHand(player1, List.of());
        Bargain drawnCard = new Bargain();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.setLife(player1, 12);
        harness.setLife(player2, 5);

        harness.castAndResolveSorcery(player2, 0, player1.getId());

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertLife(player1, 12);
        harness.assertLife(player2, 12);
        harness.assertInGraveyard(player2, "Bargain");
    }

    @Test
    @DisplayName("Cannot target self — must target an opponent")
    void cannotTargetSelf() {
        harness.setHand(player1, List.of(new Bargain()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
