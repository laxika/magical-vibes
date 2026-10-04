package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
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

@CardUsed({GlintingCreeper.class, GrizzlyBears.class, HillGiant.class})
class GlintingCreeperTest extends BaseCardTest {

    @Test
    @DisplayName("Converge puts two +1/+1 counters on Glinting Creeper for each color spent")
    void convergeDoublesCountersForEachColor() {
        harness.setHand(player1, List.of(new GlintingCreeper()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent creeper = findPermanent(player1, "Glinting Creeper");
        assertThat(creeper.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(10);
        assertThat(gqs.getEffectivePower(gd, creeper)).isEqualTo(10);
        assertThat(gqs.getEffectiveToughness(gd, creeper)).isEqualTo(10);
    }

    @Test
    @DisplayName("Repeated colors count once and colorless mana does not count")
    void convergeCountsDistinctColorsOnly() {
        harness.setHand(player1, List.of(new GlintingCreeper()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent creeper = findPermanent(player1, "Glinting Creeper");
        assertThat(creeper.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Glinting Creeper can't be blocked by creatures with power 2 or less")
    void cannotBeBlockedByPowerTwoOrLess() {
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent creeper = addCreatureReady(player1, new GlintingCreeper());
        creeper.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        creeper.setAttacking(true);

        prepareDeclareBlockers();
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(creeper);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Glinting Creeper can be blocked by a creature with power 3 or greater")
    void canBeBlockedByPowerThreeOrGreater() {
        Permanent blocker = addCreatureReady(player2, new HillGiant());
        Permanent creeper = addCreatureReady(player1, new GlintingCreeper());
        creeper.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        creeper.setAttacking(true);

        prepareDeclareBlockers();
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(creeper);

        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("One green mana and four colorless mana give only two counters")
    void singleColorPaymentGivesTwoCounters() {
        harness.setHand(player1, List.of(new GlintingCreeper()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent creeper = findPermanent(player1, "Glinting Creeper");
        assertThat(creeper.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Mana added after casting does not change converge")
    void convergeUsesManaActuallySpentAtCasting() {
        harness.setHand(player1, List.of(new GlintingCreeper()));
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.castCreature(player1, 0);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.passBothPriorities();

        Permanent creeper = findPermanent(player1, "Glinting Creeper");
        assertThat(creeper.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Entering without being cast gives no counters and the 0/0 dies")
    void enteringWithoutCastingGivesNoCounters() {
        Permanent creeper = harness.enterBattlefieldAndReturn(player1, new GlintingCreeper());

        harness.passBothPriorities();

        assertThat(creeper.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertNotOnBattlefield(player1, "Glinting Creeper");
        harness.assertInGraveyard(player1, "Glinting Creeper");
    }

    @Test
    @DisplayName("A power-two creature raised to power three by a counter can block")
    void blockingRestrictionUsesEffectivePower() {
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent creeper = addCreatureReady(player1, new GlintingCreeper());
        creeper.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        creeper.setAttacking(true);

        prepareDeclareBlockers();
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(creeper);
        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
