package com.github.laxika.magicalvibes.cards.c;

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

@CardUsed({CabalInitiate.class})
class CabalInitiateTest extends BaseCardTest {

    @Test
    @DisplayName("Discarding a card grants lifelink until end of turn")
    void discardingCardGrantsLifelink() {
        Permanent initiate = harness.addToBattlefieldAndReturn(player1, new CabalInitiate());
        harness.setHand(player1, List.of(new CabalInitiate()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, initiate, Keyword.LIFELINK)).isTrue();
        harness.assertInGraveyard(player1, "Cabal Initiate");
    }

    @Test
    @DisplayName("Lifelink granted by the ability wears off at end of turn")
    void lifelinkWearsOffAtEndOfTurn() {
        Permanent initiate = harness.addToBattlefieldAndReturn(player1, new CabalInitiate());
        harness.setHand(player1, List.of(new CabalInitiate()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

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

    @Test
    @DisplayName("The discard cost enables threshold before lifelink resolves, even while tapped")
    void discardCostEnablesThresholdBeforeResolution() {
        harness.setGraveyard(player1, graveyardWithSevenCards().subList(0, 6));
        Permanent initiate = harness.addToBattlefieldAndReturn(player1, new CabalInitiate());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new CabalInitiate());
        initiate.setTapped(true);
        initiate.setSummoningSick(true);
        harness.setHand(player1, List.of(new CabalInitiate()));

        assertThat(gqs.getEffectivePower(gd, initiate)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, initiate)).isEqualTo(1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);

        harness.assertNotInHand(player1, "Cabal Initiate");
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(7);
        assertThat(gqs.getEffectivePower(gd, initiate)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, initiate)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, initiate, Keyword.LIFELINK)).isFalse();

        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, initiate, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, other, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Threshold stops applying as soon as the graveyard drops below seven cards")
    void thresholdBonusDisappearsBelowSevenCards() {
        List<Card> cards = graveyardWithSevenCards();
        cards.add(new CabalInitiate());
        harness.setGraveyard(player1, cards);
        Permanent initiate = harness.addToBattlefieldAndReturn(player1, new CabalInitiate());

        assertThat(gqs.getEffectivePower(gd, initiate)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, initiate)).isEqualTo(3);

        harness.setGraveyard(player1, graveyardWithSevenCards().subList(0, 6));

        assertThat(gqs.getEffectivePower(gd, initiate)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, initiate)).isEqualTo(1);
    }

    @Test
    @DisplayName("Repeated activations grant lifelink without multiplying life gained from damage")
    void repeatedActivationsDoNotMultiplyLifelink() {
        Permanent initiate = harness.addToBattlefieldAndReturn(player1, new CabalInitiate());
        harness.setHand(player1, List.of(new CabalInitiate(), new CabalInitiate()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        initiate.setSummoningSick(false);
        initiate.setAttacking(true);
        harness.resolveCombatDamage();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    private List<Card> graveyardWithSevenCards() {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            cards.add(new CabalInitiate());
        }
        return cards;
    }
}
