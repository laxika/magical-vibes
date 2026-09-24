package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CabalInitiate.class, GrizzlyBears.class})
class CabalInitiateTest extends BaseCardTest {

    @Test
    @DisplayName("Discarding a card grants lifelink until end of turn")
    void discardingCardGrantsLifelink() {
        Permanent initiate = harness.addToBattlefieldAndReturn(player1, new CabalInitiate());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, initiate, Keyword.LIFELINK)).isTrue();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Lifelink granted by the ability wears off at end of turn")
    void lifelinkWearsOffAtEndOfTurn() {
        Permanent initiate = harness.addToBattlefieldAndReturn(player1, new CabalInitiate());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, initiate, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Threshold gives this creature +1/+2")
    void thresholdBonus() {
        harness.setGraveyard(player1, graveyardWithSevenCards());
        Permanent initiate = harness.addToBattlefieldAndReturn(player1, new CabalInitiate());

        assertThat(gqs.getEffectivePower(gd, initiate)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, initiate)).isEqualTo(3);
    }

    @Test
    @DisplayName("Threshold does not count an opponent's graveyard")
    void opponentGraveyardDoesNotCount() {
        harness.setGraveyard(player2, graveyardWithSevenCards());
        Permanent initiate = harness.addToBattlefieldAndReturn(player1, new CabalInitiate());

        assertThat(gqs.getEffectivePower(gd, initiate)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, initiate)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate without a card to discard")
    void cannotActivateWithoutCardInHand() {
        harness.addToBattlefield(player1, new CabalInitiate());
        harness.setHand(player1, List.of());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private List<Card> graveyardWithSevenCards() {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            cards.add(new GrizzlyBears());
        }
        return cards;
    }
}
