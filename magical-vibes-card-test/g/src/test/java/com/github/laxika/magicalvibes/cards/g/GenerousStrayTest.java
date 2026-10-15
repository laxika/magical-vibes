package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GenerousStray.class, Forest.class})
class GenerousStrayTest extends BaseCardTest {

    @Test
    @DisplayName("ETB draws a card")
    void etbDrawsCard() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new GenerousStray()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        int handSizeBeforeTrigger = gd.playerHands.get(player1.getId()).size();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBeforeTrigger + 1);
        harness.assertOnBattlefield(player1, "Generous Stray");
    }

    @Test
    @DisplayName("Entering without casting draws only for the creature's controller")
    void enteringWithoutCastingDrawsForController() {
        GenerousStray drawnCard = new GenerousStray();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(drawnCard));

        harness.enterBattlefieldAndReturn(player2, new GenerousStray());

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(drawnCard);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("The draw trigger resolves after Generous Stray leaves the battlefield")
    void drawTriggerSurvivesSourceLeaving() {
        GenerousStray drawnCard = new GenerousStray();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawnCard));
        var stray = harness.enterBattlefieldAndReturn(player1, new GenerousStray());

        gd.playerBattlefields.get(player1.getId()).remove(stray);
        gd.playerGraveyards.get(player1.getId()).add(stray.getCard());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }
}
