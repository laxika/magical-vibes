package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PotionOfHealing.class})
class PotionOfHealingTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield and draws a card")
    void entersAndDrawsCard() {
        Card drawnCard = new PotionOfHealing();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.castFromHand(player1, new PotionOfHealing(), "{1}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Pays white, taps, sacrifices itself, and gains 3 life")
    void sacrificesItselfToGainLife() {
        Permanent potion = harness.addToBattlefieldAndReturn(player1, new PotionOfHealing());
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, null);

        assertThat(potion.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(potion);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(potion.getCard());
        harness.assertLife(player1, 20);
        harness.passBothPriorities();

        harness.assertLife(player1, 23);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(potion);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(potion.getCard());
    }

    @Test
    @DisplayName("Cannot activate without white mana")
    void cannotActivateWithoutWhiteMana() {
        harness.addToBattlefield(player1, new PotionOfHealing());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate a tapped Potion or sacrifice it on a failed activation")
    void cannotActivateWhileTapped() {
        Permanent potion = harness.addToBattlefieldAndReturn(player1, new PotionOfHealing());
        potion.tap();
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(potion);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(potion.getCard());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Sacrificing a newly entered Potion does not remove its pending draw trigger")
    void drawTriggerResolvesAfterSacrifice() {
        Card drawnCard = new PotionOfHealing();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.castFromHand(player1, new PotionOfHealing(), "{1}{W}");
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.assertLife(player1, 23);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Can activate during an opponent's turn and only its controller gains life")
    void activatesDuringOpponentsTurn() {
        harness.addToBattlefield(player1, new PotionOfHealing());
        harness.setLife(player1, 10);
        harness.setLife(player2, 15);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 13);
        harness.assertLife(player2, 15);
    }
}
