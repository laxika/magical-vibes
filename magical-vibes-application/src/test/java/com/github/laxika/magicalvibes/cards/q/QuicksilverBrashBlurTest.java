package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(QuicksilverBrashBlur.class)
class QuicksilverBrashBlurTest extends BaseCardTest {

    @Test
    @DisplayName("Quicksilver may begin the game on the battlefield from the opening hand")
    void mayBeginGameOnBattlefieldFromOpeningHand() {
        GameTestHarness openingHarness = new GameTestHarness();
        openingHarness.setHand(openingHarness.getPlayer1(), List.of(new QuicksilverBrashBlur()));
        openingHarness.skipMulligan();

        assertThat(openingHarness.getGameData().interaction.isAwaitingInput()).isTrue();

        openingHarness.handleMayAbilityChosen(openingHarness.getPlayer1(), true);

        assertThat(openingHarness.getGameData().playerHands
                .get(openingHarness.getPlayer1().getId())).isEmpty();
        assertThat(openingHarness.getGameData().playerBattlefields
                .get(openingHarness.getPlayer1().getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof QuicksilverBrashBlur);
    }

    @Test
    @DisplayName("Declining Quicksilver's opening-hand ability keeps it in hand")
    void decliningOpeningHandAbilityKeepsCardInHand() {
        GameTestHarness openingHarness = new GameTestHarness();
        QuicksilverBrashBlur quicksilver = new QuicksilverBrashBlur();
        openingHarness.setHand(openingHarness.getPlayer1(), List.of(quicksilver));
        openingHarness.skipMulligan();

        openingHarness.handleMayAbilityChosen(openingHarness.getPlayer1(), false);

        assertThat(openingHarness.getGameData().playerHands
                .get(openingHarness.getPlayer1().getId())).containsExactly(quicksilver);
        assertThat(openingHarness.getGameData().playerBattlefields
                .get(openingHarness.getPlayer1().getId()))
                .noneMatch(permanent -> permanent.getCard() instanceof QuicksilverBrashBlur);
    }

    @Test
    @DisplayName("Entry-turn power-up costs four generic mana and grants both counters")
    void powerUpIsDiscountedDuringEntryTurn() {
        Permanent quicksilver = harness.enterBattlefieldAndReturn(player1, new QuicksilverBrashBlur());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(quicksilver.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(quicksilver.getCounterCount(CounterType.DOUBLE_STRIKE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, quicksilver, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("A Quicksilver that started the game on the battlefield does not get an entry-turn discount")
    void pregameQuicksilverDoesNotGetEntryTurnDiscount() {
        GameTestHarness openingHarness = new GameTestHarness();
        openingHarness.setHand(openingHarness.getPlayer1(), List.of(new QuicksilverBrashBlur()));
        openingHarness.skipMulligan();
        openingHarness.handleMayAbilityChosen(openingHarness.getPlayer1(), true);
        openingHarness.addMana(openingHarness.getPlayer1(), ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> openingHarness.activateAbility(
                openingHarness.getPlayer1(), 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Power-up costs its full activation cost after the entry turn")
    void powerUpIsNotDiscountedAfterEntryTurn() {
        Permanent quicksilver = addCreatureReady(player1, new QuicksilverBrashBlur());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(quicksilver.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(quicksilver.getCounterCount(CounterType.DOUBLE_STRIKE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Quicksilver's power-up can be activated only once")
    void powerUpCanBeActivatedOnlyOnce() {
        addCreatureReady(player1, new QuicksilverBrashBlur());
        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once");
    }
}
