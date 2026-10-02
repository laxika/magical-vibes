package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AlchemistsApprentice.class})
class AlchemistsApprenticeTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing Alchemist's Apprentice draws a card")
    void sacrificeDrawsCard() {
        harness.addToBattlefield(player1, new AlchemistsApprentice());

        GameData gd = harness.getGameData();
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Alchemist's Apprentice");
        harness.assertInGraveyard(player1, "Alchemist's Apprentice");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Alchemist's Apprentice can be sacrificed while summoning sick")
    void canActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new AlchemistsApprentice());

        GameData gd = harness.getGameData();
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Alchemist's Apprentice");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }
    @Test
    @DisplayName("Sacrifice is paid immediately but the card is drawn only on resolution")
    void sacrificeIsCostAndDrawUsesStack() {
        harness.addToBattlefield(player1, new AlchemistsApprentice());
        harness.setHand(player1, List.of());
        AlchemistsApprentice topCard = new AlchemistsApprentice();
        harness.setLibrary(player1, List.of(topCard));

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Alchemist's Apprentice");
        harness.assertInGraveyard(player1, "Alchemist's Apprentice");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A tapped Apprentice can be sacrificed on the opponent's turn")
    void tappedApprenticeCanActivateOnOpponentsTurn() {
        Permanent apprentice = harness.addToBattlefieldAndReturn(player1, new AlchemistsApprentice());
        apprentice.tap();
        harness.forceActivePlayer(player2);
        harness.setHand(player1, List.of());
        AlchemistsApprentice topCard = new AlchemistsApprentice();
        harness.setLibrary(player1, List.of(topCard));
        int opponentHandBefore = gd.playerHands.get(player2.getId()).size();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Alchemist's Apprentice");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandBefore);
    }
}
