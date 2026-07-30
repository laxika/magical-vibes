package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
}
