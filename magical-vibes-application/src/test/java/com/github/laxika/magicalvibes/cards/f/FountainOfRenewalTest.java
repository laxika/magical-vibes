package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FountainOfRenewalTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 1 life at the beginning of its controller's upkeep")
    void gainsLifeAtControllerUpkeep() {
        harness.addToBattlefield(player1, new FountainOfRenewal());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    @DisplayName("Sacrificing it draws a card")
    void sacrificeAbilityDrawsCard() {
        harness.addToBattlefield(player1, new FountainOfRenewal());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        assertThat(gameData.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        harness.assertNotOnBattlefield(player1, "Fountain of Renewal");
        harness.assertInGraveyard(player1, "Fountain of Renewal");
    }
}
