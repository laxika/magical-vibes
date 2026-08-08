package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CallousDeceiver;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
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

class SkullmaneBakuTest extends BaseCardTest {

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

        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getName().equals("Skullmane Baku"));
        assertThat(baku.getCounterCount(CounterType.KI)).isZero();
    }

    @Test
    @DisplayName("Removing one ki counter shrinks the target by -1/-1")
    void removingOneKiCounterShrinksTarget() {
        Permanent baku = addBaku();
        baku.setCounterCount(CounterType.KI, 3);
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, giant.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(2);
        assertThat(baku.getCounterCount(CounterType.KI)).isEqualTo(2);
        assertThat(baku.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Removing two ki counters kills a 2/2")
    void removingTwoKiCountersKillsSmallCreature() {
        Permanent baku = addBaku();
        baku.setCounterCount(CounterType.KI, 2);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 2, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bears);
        assertThat(baku.getCounterCount(CounterType.KI)).isZero();
    }

    @Test
    @DisplayName("The -X/-X wears off at end of turn")
    void shrinkWearsOffAtEndOfTurn() {
        Permanent baku = addBaku();
        baku.setCounterCount(CounterType.KI, 1);
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, giant.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(3);
    }

    @Test
    @DisplayName("X cannot exceed the ki counters on the creature")
    void rejectsXAboveKiCounterCount() {
        Permanent baku = addBaku();
        baku.setCounterCount(CounterType.KI, 1);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A land is an illegal target")
    void rejectsNonCreatureTarget() {
        Permanent baku = addBaku();
        baku.setCounterCount(CounterType.KI, 2);
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addBaku() {
        Permanent baku = harness.addToBattlefieldAndReturn(player1, new SkullmaneBaku());
        // The {T} in the shrink cost needs a creature that has been under its controller's control
        // since their most recent turn began (CR 302.6).
        baku.setSummoningSick(false);
        return baku;
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
