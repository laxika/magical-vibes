package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CephalidAristocrat;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SkywingAven.class, CephalidAristocrat.class})
class SkywingAvenTest extends BaseCardTest {

    @Test
    void discardingACardReturnsSkywingAvenToItsOwnersHand() {
        harness.addToBattlefield(player1, new SkywingAven());
        harness.setHand(player1, List.of(new CephalidAristocrat()));

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardCostChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Skywing Aven");
        harness.assertNotOnBattlefield(player1, "Skywing Aven");
        harness.assertInGraveyard(player1, "Cephalid Aristocrat");
    }

    @Test
    void returnsToOwnersHandWhenControlledByAnotherPlayer() {
        SkywingAven skywingCard = new SkywingAven();
        skywingCard.setOwnerId(player1.getId());
        Permanent skywing = harness.addToBattlefieldAndReturn(player2, skywingCard);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new CephalidAristocrat()));

        harness.activateAbility(player2, 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardCostChoice.class);
        harness.handleCardChosen(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(skywing.getCard().getId()));
        assertThat(gd.playerHands.get(player2.getId()))
                .noneMatch(card -> card.getId().equals(skywing.getCard().getId()));
        harness.assertNotOnBattlefield(player2, "Skywing Aven");
        harness.assertInGraveyard(player2, "Cephalid Aristocrat");
    }

    @Test
    void cannotActivateWithoutACardToDiscard() {
        harness.addToBattlefield(player1, new SkywingAven());
        harness.setHand(player1, List.of());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
