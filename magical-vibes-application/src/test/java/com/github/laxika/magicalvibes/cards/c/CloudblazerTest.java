package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Cloudblazer.class, Forest.class})
class CloudblazerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gains 2 life and draws two cards")
    void etbGainsLifeAndDrawsCards() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of(new Cloudblazer()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        int lifeBefore = gd.getLife(player1.getId());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Entering without being cast benefits only its controller")
    void enteringWithoutCastingBenefitsController() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));
        int controllerLifeBefore = gd.getLife(player2.getId());
        int opponentLifeBefore = gd.getLife(player1.getId());

        harness.enterBattlefieldAndReturn(player2, new Cloudblazer());

        assertThat(gd.getLife(player2.getId())).isEqualTo(controllerLifeBefore);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(controllerLifeBefore + 2);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(opponentLifeBefore);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Drawing the last two cards does not lose the game")
    void drawingLastTwoCardsDoesNotLose() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        int lifeBefore = gd.getLife(player1.getId());

        harness.enterBattlefieldAndReturn(player1, new Cloudblazer());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
        assertThat(gd.stack).isEmpty();
    }
}
