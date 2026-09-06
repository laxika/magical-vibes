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
        harness.passBothPriorities();

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
