package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JaceThePerfectedMind.class, GrizzlyBears.class})
class JaceThePerfectedMindTest extends BaseCardTest {

    @Test
    @DisplayName("+1 gives up to one target creature -3/-0 until your next turn")
    void plusOneShrinksTargetUntilYourNextTurn() {
        Permanent jace = addReadyJace(player1, 4);
        Permanent bear = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(bear.getId()));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(-1);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    @DisplayName("+1 may be activated without a target")
    void plusOneAllowsNoTarget() {
        Permanent jace = addReadyJace(player1, 4);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of());
        harness.passBothPriorities();

        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    @DisplayName("-2 mills three, then draws one below the graveyard threshold")
    void minusTwoMillsAndDrawsOneBelowThreshold() {
        Permanent jace = addReadyJace(player1, 4);
        stockLibrary(player1, 30);
        stockLibrary(player2, 30);

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(27);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    @DisplayName("-2 draws three when milling makes a graveyard reach twenty cards")
    void minusTwoDrawsThreeAtThreshold() {
        Permanent jace = addReadyJace(player1, 4);
        stockLibrary(player1, 30);
        stockLibrary(player2, 30);
        harness.setGraveyard(player2, filler(19));

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(22);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    @DisplayName("-X mills three times X cards")
    void minusXMillsThreeTimesX() {
        Permanent jace = addReadyJace(player1, 6);
        stockLibrary(player2, 30);

        harness.activateAbility(player1, 0, 2, 2, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(24);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(6);
        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    void plusOneCannotTargetTwoCreatures() {
        Permanent jace = addReadyJace(player1, 5);
        Permanent first = addCreatureReady(player2, new GrizzlyBears());
        Permanent second = addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    void minusXCannotExceedAvailableLoyalty() {
        Permanent jace = addReadyJace(player1, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, 6, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    void plusOnePersistsThroughOpponentsTurnAndExpiresOnControllersNextTurn() {
        addReadyJace(player1, 5);
        Permanent bear = addCreatureReady(player2, new GrizzlyBears());
        stockLibrary(player1, 10);
        stockLibrary(player2, 10);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(bear.getId()));
        harness.passBothPriorities();
        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(-1);

        harness.passUntilWithNoAttackers(player1, TurnStep.UPKEEP);
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
    }

    @Test
    void minusTwoDrawsThreeAtExactlyTwentyInControllersGraveyard() {
        addReadyJace(player1, 5);
        stockLibrary(player1, 30);
        stockLibrary(player2, 30);
        harness.setGraveyard(player1, filler(20));

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }

    @Test
    void minusTwoCanMillControllerToExactlyTwentyBeforeDrawing() {
        addReadyJace(player1, 5);
        stockLibrary(player1, 30);
        harness.setGraveyard(player1, filler(17));

        harness.activateAbility(player1, 0, 1, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(20);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(24);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }

    @Test
    void minusTwoDoesNotCombineSeparateGraveyardsForThreshold() {
        addReadyJace(player1, 5);
        stockLibrary(player1, 30);
        stockLibrary(player2, 30);
        harness.setGraveyard(player1, filler(10));
        harness.setGraveyard(player2, filler(16));

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(19);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void minusXAllowsZero() {
        Permanent jace = addReadyJace(player1, 5);
        stockLibrary(player2, 10);

        harness.activateAbility(player1, 0, 2, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(10);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    void minusXResolvesAfterSpendingAllLoyaltyAndMillsOnlyAvailableCards() {
        addReadyJace(player1, 5);
        stockLibrary(player2, 4);

        harness.activateAbility(player1, 0, 2, 5, player2.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Jace, the Perfected Mind");
        harness.assertInGraveyard(player1, "Jace, the Perfected Mind");
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(4);
    }

    @Test
    void castingWithManaEntersWithFiveLoyalty() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new JaceThePerfectedMind()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castPlaneswalker(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Jace, the Perfected Mind")
                .getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        harness.assertLife(player1, 20);
    }

    @Test
    void castingWithLifeEntersWithThreeLoyalty() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new JaceThePerfectedMind()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castPlaneswalker(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Jace, the Perfected Mind")
                .getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        harness.assertLife(player1, 18);
    }

    private List<Card> filler(int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new GrizzlyBears());
        }
        return cards;
    }

    private void stockLibrary(Player player, int count) {
        harness.setLibrary(player, filler(count));
        harness.setHand(player, List.of());
    }

    private Permanent addReadyJace(Player player, int loyalty) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new JaceThePerfectedMind());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
