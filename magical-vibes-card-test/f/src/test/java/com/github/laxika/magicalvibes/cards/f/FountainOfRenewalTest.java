package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FountainOfRenewal.class})
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

    @Test
    @DisplayName("Does not gain life during the opponent's upkeep")
    void doesNotTriggerDuringOpponentUpkeep() {
        harness.addToBattlefield(player1, new FountainOfRenewal());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Sacrifice is paid immediately and drawing waits for resolution even when tapped")
    void tappedFountainSacrificesAsCostBeforeDrawing() {
        harness.addToBattlefieldAndReturn(player1, new FountainOfRenewal()).tap();
        harness.setHand(player1, List.of());
        FountainOfRenewal drawnCard = new FountainOfRenewal();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Fountain of Renewal");
        harness.assertInGraveyard(player1, "Fountain of Renewal");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    @DisplayName("Cannot sacrifice the Fountain without paying three mana")
    void insufficientManaDoesNotSacrificeFountain() {
        harness.addToBattlefield(player1, new FountainOfRenewal());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Fountain of Renewal");
        harness.assertNotInGraveyard(player1, "Fountain of Renewal");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Upkeep trigger still gains life after sacrificing its source in response")
    void upkeepTriggerResolvesAfterSourceIsSacrificed() {
        harness.addToBattlefield(player1, new FountainOfRenewal());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of());
        FountainOfRenewal drawnCard = new FountainOfRenewal();
        harness.setLibrary(player1, List.of(drawnCard));

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.assertLife(player1, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, null);

        harness.assertInGraveyard(player1, "Fountain of Renewal");
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        harness.assertLife(player1, 20);

        resolveAllTriggers();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 20);
    }
}
