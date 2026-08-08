package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.CallousDeceiver;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.ReachThroughMists;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BlademaneBakuTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a Spirit spell offers a ki counter, which is placed when accepted")
    void spiritSpellAddsKiCounter() {
        Permanent baku = addBaku();
        prepareMainPhase();
        harness.setHand(player1, List.of(new CallousDeceiver()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(baku.getCounterCount(CounterType.KI)).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining the Arcane trigger leaves the ki counter off")
    void decliningLeavesNoKiCounter() {
        Permanent baku = addBaku();
        prepareMainPhase();
        harness.setHand(player1, List.of(new ReachThroughMists()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(baku.getCounterCount(CounterType.KI)).isZero();
    }

    @Test
    @DisplayName("Casting a spell that is neither Spirit nor Arcane does not trigger")
    void unrelatedSpellDoesNotTrigger() {
        Permanent baku = addBaku();
        prepareMainPhase();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getName().equals("Blademane Baku"));
        assertThat(baku.getCounterCount(CounterType.KI)).isZero();
    }

    @Test
    @DisplayName("Removing two ki counters gives +4/+0")
    void removingTwoKiCountersBoostsByFour() {
        Permanent baku = addBaku();
        baku.setCounterCount(CounterType.KI, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 2, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, baku)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, baku)).isEqualTo(1);
        assertThat(baku.getCounterCount(CounterType.KI)).isEqualTo(1);
        assertThat(baku.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent baku = addBaku();
        baku.setCounterCount(CounterType.KI, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, baku)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, baku)).isEqualTo(1);
    }

    @Test
    @DisplayName("X cannot exceed the ki counters on the creature")
    void rejectsXAboveKiCounterCount() {
        Permanent baku = addBaku();
        baku.setCounterCount(CounterType.KI, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addBaku() {
        return harness.addToBattlefieldAndReturn(player1, new BlademaneBaku());
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
