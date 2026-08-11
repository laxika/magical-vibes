package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LochMareTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield with three -1/-1 counters")
    void entersWithThreeMinusOneMinusOneCounters() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new LochMare()));
        addMana(player1, ManaColor.COLORLESS, 1);
        addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Loch Mare").getCounterCount(CounterType.MINUS_ONE_MINUS_ONE))
                .isEqualTo(3);
    }

    @Test
    @DisplayName("Removing one -1/-1 counter draws a card")
    void drawAbilityRemovesOneCounterAndDraws() {
        Permanent lochMare = addReadyLochMare(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        addMana(player1, ManaColor.COLORLESS, 1);
        addMana(player1, ManaColor.BLUE, 1);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(lochMare.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Removing two -1/-1 counters taps a creature and puts a stun counter on it")
    void tapAbilityRemovesTwoCountersAndStunsTarget() {
        Permanent lochMare = addReadyLochMare(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        addMana(player1, ManaColor.COLORLESS, 2);
        addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(lochMare.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(target.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.STUN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Tap ability rejects a noncreature target")
    void tapAbilityRejectsNoncreatureTarget() {
        addReadyLochMare(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        addMana(player1, ManaColor.COLORLESS, 2);
        addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    private Permanent addReadyLochMare(Player player) {
        Permanent lochMare = new Permanent(new LochMare());
        lochMare.setSummoningSick(false);
        lochMare.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 3);
        gd.playerBattlefields.get(player.getId()).add(lochMare);
        return lochMare;
    }

    private void addMana(Player player, ManaColor color, int amount) {
        harness.addMana(player, color, amount);
    }
}
