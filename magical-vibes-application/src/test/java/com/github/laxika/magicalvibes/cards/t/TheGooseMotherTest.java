package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheGooseMother.class})
class TheGooseMotherTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with X counters and half X Food tokens rounded up")
    void entersWithCountersAndFood() {
        Permanent goose = castGoose(3);

        assertThat(goose.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(countPermanents(player1, "Food")).isEqualTo(2);
    }

    @Test
    @DisplayName("Attacking may sacrifice a Food to draw a card")
    void attackingMaySacrificeFoodToDraw() {
        harness.setLibrary(player1, List.of(new TheGooseMother()));
        Permanent goose = castGoose(2);
        goose.setSummoningSick(false);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        Permanent food = findPermanent(player1, "Food");

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(goose)));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, food.getId());

        assertThat(countPermanents(player1, "Food")).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Declining the attack trigger leaves Food and draws nothing")
    void decliningAttackTriggerDoesNothing() {
        harness.setLibrary(player1, List.of(new TheGooseMother()));
        Permanent goose = castGoose(2);
        goose.setSummoningSick(false);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(goose)));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(countPermanents(player1, "Food")).isOne();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    @DisplayName("X zero creates no counters or Food")
    void zeroXCreatesNoCountersOrFood() {
        Permanent goose = castGoose(0);

        assertThat(goose.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(countPermanents(player1, "Food")).isZero();
    }

    @Test
    @DisplayName("Even X creates exactly half as many Food tokens")
    void evenXCreatesHalfAsManyFoodTokens() {
        Permanent goose = castGoose(4);

        assertThat(goose.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(countPermanents(player1, "Food")).isEqualTo(2);
    }

    @Test
    @DisplayName("Created Food can be tapped and sacrificed for two mana to gain three life")
    void foodTokenCanGainLife() {
        castGoose(1);
        Permanent food = findPermanent(player1, "Food");
        harness.setLife(player1, 10);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(food), null, null);

        assertThat(countPermanents(player1, "Food")).isZero();
        harness.assertLife(player1, 10);
        harness.passBothPriorities();
        harness.assertLife(player1, 13);
    }

    @Test
    @DisplayName("Attacking without Food cannot draw a card")
    void attackingWithoutFoodDoesNotDraw() {
        harness.setLibrary(player1, List.of(new TheGooseMother()));
        Permanent goose = castGoose(0);
        goose.setSummoningSick(false);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(goose)));
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    private Permanent castGoose(int xValue) {
        harness.setHand(player1, List.of(new TheGooseMother()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, xValue);
        gs.playCard(gd, player1, 0, xValue, null, null);
        resolveAllTriggers();
        return findPermanent(player1, "The Goose Mother");
    }
}
