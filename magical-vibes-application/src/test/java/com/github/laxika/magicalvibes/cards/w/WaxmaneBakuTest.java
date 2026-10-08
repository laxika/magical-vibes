package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.CallousDeceiver;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.ReachThroughMists;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({WaxmaneBaku.class, CallousDeceiver.class, ReachThroughMists.class,
        GrizzlyBears.class, Forest.class})
class WaxmaneBakuTest extends BaseCardTest {

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

        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getName().equals("Waxmane Baku"));
        assertThat(baku.getCounterCount(CounterType.KI)).isZero();
    }

    @Test
    @DisplayName("Removing two ki counters taps two target creatures")
    void removingTwoKiCountersTapsTwoCreatures() {
        Permanent baku = addBaku();
        baku.setCounterCount(CounterType.KI, 2);
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, 2, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(baku.getCounterCount(CounterType.KI)).isZero();
    }

    @Test
    @DisplayName("X cannot exceed the ki counters on the creature")
    void rejectsXAboveKiCounterCount() {
        Permanent baku = addBaku();
        baku.setCounterCount(CounterType.KI, 1);
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, 2, List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A land is an illegal target")
    void rejectsNonCreatureTarget() {
        Permanent baku = addBaku();
        baku.setCounterCount(CounterType.KI, 1);
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, 1, List.of(forest.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Requires exactly X creature targets")
    void rejectsFewerTargetsThanKiCountersRemoved() {
        Permanent baku = addBaku();
        baku.setCounterCount(CounterType.KI, 2);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, 2, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Accepting an Arcane cast trigger adds a ki counter before the spell resolves")
    void arcaneSpellAddsKiCounterBeforeResolving() {
        Permanent baku = addBaku();
        prepareMainPhase();
        harness.setHand(player1, List.of(new ReachThroughMists()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(baku.getCounterCount(CounterType.KI)).isEqualTo(1);
        assertThat(gd.stack).anyMatch(entry -> entry.getCard() instanceof ReachThroughMists);
    }

    @Test
    @DisplayName("An opponent's Arcane spell does not trigger Waxmane Baku")
    void opponentsArcaneSpellDoesNotTrigger() {
        Permanent baku = addBaku();
        prepareMainPhase();
        harness.setHand(player2, List.of(new ReachThroughMists()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castInstant(player2, 0);

        assertThat(gd.stack).noneMatch(entry -> entry.getCard() instanceof WaxmaneBaku);
        assertThat(baku.getCounterCount(CounterType.KI)).isZero();
    }

    @Test
    @DisplayName("X can be zero with no ki counters and no targets")
    void zeroCountersAndZeroTargetsAreLegal() {
        Permanent baku = addBaku();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, 0, List.of());
        harness.passBothPriorities();

        assertThat(baku.getCounterCount(CounterType.KI)).isZero();
        assertThat(baku.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped, summoning-sick Baku can activate repeatedly and spend fewer than all counters")
    void tappedBakuCanActivateRepeatedly() {
        Permanent baku = addBaku();
        baku.setTapped(true);
        baku.setSummoningSick(true);
        baku.setCounterCount(CounterType.KI, 3);
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, 1, List.of(first.getId()));
        assertThat(baku.getCounterCount(CounterType.KI)).isEqualTo(2);
        assertThat(first.isTapped()).isFalse();
        harness.activateAbilityWithMultiTargets(player1, 0, 0, 1, List.of(second.getId()));
        assertThat(baku.getCounterCount(CounterType.KI)).isEqualTo(1);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The same creature cannot be selected twice for one activation")
    void rejectsDuplicateTargets() {
        Permanent baku = addBaku();
        baku.setCounterCount(CounterType.KI, 2);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, 2, List.of(target.getId(), target.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(baku.getCounterCount(CounterType.KI)).isEqualTo(2);
    }

    @Test
    @DisplayName("The remaining legal target is tapped when another target leaves the battlefield")
    void remainingTargetIsTapped() {
        Permanent baku = addBaku();
        baku.setCounterCount(CounterType.KI, 2);
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, 2, List.of(first.getId(), second.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(first);
        gd.playerGraveyards.get(player2.getId()).add(first.getCard());
        harness.passBothPriorities();

        assertThat(second.isTapped()).isTrue();
        assertThat(baku.getCounterCount(CounterType.KI)).isZero();
    }

    @Test
    @DisplayName("Removing the source does not stop its activated ability from tapping the target")
    void abilityResolvesWithoutSource() {
        Permanent baku = addBaku();
        baku.setCounterCount(CounterType.KI, 1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, 1, List.of(target.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(baku);
        gd.playerGraveyards.get(player1.getId()).add(baku.getCard());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    private Permanent addBaku() {
        return harness.addToBattlefieldAndReturn(player1, new WaxmaneBaku());
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
