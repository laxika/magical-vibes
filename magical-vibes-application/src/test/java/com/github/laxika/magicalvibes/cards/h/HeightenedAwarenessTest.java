package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GulfSquid;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GulfSquid.class, HazyHomunculus.class, HeightenedAwareness.class})
class HeightenedAwarenessTest extends BaseCardTest {

    private void advanceToDraw(Player activePlayer) {
        gd.turnNumber = 2;
        advanceToUpkeep(activePlayer);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Entering the battlefield discards the controller's hand")
    void enteringDiscardsControllerHand() {
        harness.setHand(player1, List.of(new HeightenedAwareness(), new HazyHomunculus(), new GulfSquid()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(card -> card instanceof HazyHomunculus);
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(card -> card instanceof GulfSquid);
    }

    @Test
    @DisplayName("Discards the controller's hand as the enchantment enters")
    void discardsHandBeforePlayersReceivePriority() {
        harness.setHand(player1, List.of(new HeightenedAwareness(), new HazyHomunculus(), new GulfSquid()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Controller draws an additional card during their draw step")
    void controllerDrawsAdditionalCard() {
        harness.addToBattlefield(player1, new HeightenedAwareness());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new HazyHomunculus(), new GulfSquid()));

        advanceToDraw(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Additional draw does not apply during an opponent's draw step")
    void additionalDrawDoesNotApplyToOpponent() {
        harness.addToBattlefield(player1, new HeightenedAwareness());
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new HazyHomunculus()));

        advanceToDraw(player2);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }
}
