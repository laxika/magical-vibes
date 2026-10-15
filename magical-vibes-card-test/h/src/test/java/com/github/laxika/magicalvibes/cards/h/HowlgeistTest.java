package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.m.MoorlandInquisitor;
import com.github.laxika.magicalvibes.cards.n.NettleSwine;
import com.github.laxika.magicalvibes.cards.t.ThunderousWrath;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Howlgeist.class, MoorlandInquisitor.class, NettleSwine.class, ThunderousWrath.class})
class HowlgeistTest extends BaseCardTest {

    @Test
    @DisplayName("Can't be blocked by a creature with less power")
    void cannotBeBlockedByLowerPower() {
        Permanent blocker = addCreatureReady(player2, new MoorlandInquisitor());
        Permanent howlgeist = addCreatureReady(player1, new Howlgeist());
        howlgeist.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(howlgeist);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power too low");
    }

    @Test
    @DisplayName("Can be blocked by a creature with equal power")
    void canBeBlockedByEqualPower() {
        Permanent blocker = addCreatureReady(player2, new NettleSwine());
        Permanent howlgeist = addCreatureReady(player1, new Howlgeist());
        howlgeist.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(howlgeist);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Undying does not return a creature that died with a +1/+1 counter")
    void doesNotReturnWithPlusOneCounter() {
        Permanent howlgeist = harness.addToBattlefieldAndReturn(player1, new Howlgeist());
        howlgeist.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player1, List.of(new ThunderousWrath()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castAndResolveInstant(player1, 0, howlgeist.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Howlgeist");
        harness.assertInGraveyard(player1, "Howlgeist");
    }

    @Test
    @DisplayName("A blocker whose power is raised above Howlgeist's power can block")
    void canBeBlockedByHigherEffectivePower() {
        Permanent blocker = addCreatureReady(player2, new NettleSwine());
        blocker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent howlgeist = addCreatureReady(player1, new Howlgeist());
        howlgeist.setAttacking(true);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("A blocker reduced below Howlgeist's power cannot block")
    void cannotBeBlockedByReducedEffectivePower() {
        Permanent blocker = addCreatureReady(player2, new NettleSwine());
        blocker.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        Permanent howlgeist = addCreatureReady(player1, new Howlgeist());
        howlgeist.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power too low");
    }

    @Test
    @DisplayName("Undying still returns a creature that died with a -1/-1 counter")
    void returnsWithMinusOneCounter() {
        Permanent howlgeist = harness.addToBattlefieldAndReturn(player1, new Howlgeist());
        howlgeist.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        harness.setHand(player1, List.of(new ThunderousWrath()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castAndResolveInstant(player1, 0, howlgeist.getId());
        resolveAllTriggers();

        Permanent returned = findPermanent(player1, "Howlgeist");
        assertThat(returned.getId()).isNotEqualTo(howlgeist.getId());
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(returned.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        harness.assertNotInGraveyard(player1, "Howlgeist");
    }

    @Test
    @DisplayName("A stolen Howlgeist returns under its owner's control")
    void undyingReturnsToOwner() {
        Permanent howlgeist = harness.addToBattlefieldAndReturn(player2, new Howlgeist());
        gd.stolenCreatures.put(howlgeist.getId(), player1.getId());
        harness.setHand(player1, List.of(new ThunderousWrath()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castAndResolveInstant(player1, 0, howlgeist.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Howlgeist");
        Permanent returned = findPermanent(player1, "Howlgeist");
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertNotInGraveyard(player1, "Howlgeist");
    }

    @Test
    @DisplayName("Howlgeist does not return a second time while its undying counter remains")
    void undyingDoesNotReturnTwice() {
        Permanent howlgeist = harness.addToBattlefieldAndReturn(player1, new Howlgeist());
        harness.setHand(player1, List.of(new ThunderousWrath(), new ThunderousWrath()));
        harness.addMana(player1, ManaColor.RED, 12);

        harness.castAndResolveInstant(player1, 0, howlgeist.getId());
        resolveAllTriggers();
        Permanent returned = findPermanent(player1, "Howlgeist");
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        harness.castAndResolveInstant(player1, 0, returned.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Howlgeist");
        harness.assertInGraveyard(player1, "Howlgeist");
    }

    @Test
    @DisplayName("Undying returns it with a +1/+1 counter, tightening the block restriction")
    void undyingReturnRaisesBlockThreshold() {
        Permanent howlgeist = harness.addToBattlefieldAndReturn(player1, new Howlgeist());
        harness.setHand(player1, List.of(new ThunderousWrath()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castAndResolveInstant(player1, 0, howlgeist.getId());
        resolveAllTriggers();

        Permanent returned = findPermanent(player1, "Howlgeist");
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, returned)).isEqualTo(5);

        Permanent blocker = addCreatureReady(player2, new NettleSwine());
        returned.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(returned);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power too low");
    }
}
