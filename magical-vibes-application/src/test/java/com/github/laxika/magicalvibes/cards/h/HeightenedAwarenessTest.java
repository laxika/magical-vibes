package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HeightenedAwarenessTest extends BaseCardTest {

    private void advanceToDraw(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Entering the battlefield discards the controller's hand")
    void enteringDiscardsControllerHand() {
        harness.setHand(player1, List.of(new HeightenedAwareness(), new Forest(), new Mountain()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(card -> card instanceof Forest);
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(card -> card instanceof Mountain);
    }

    @Test
    @DisplayName("Controller draws an additional card during their draw step")
    void controllerDrawsAdditionalCard() {
        harness.addToBattlefield(player1, new HeightenedAwareness());
        harness.setHand(player1, List.of());

        advanceToDraw(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Additional draw does not apply during an opponent's draw step")
    void additionalDrawDoesNotApplyToOpponent() {
        harness.addToBattlefield(player1, new HeightenedAwareness());
        harness.setHand(player2, List.of());

        advanceToDraw(player2);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }
}
