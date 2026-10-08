package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.n.NestInvader;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ZulaportEnforcer.class, NestInvader.class})
class ZulaportEnforcerTest extends BaseCardTest {

    @Test
    @DisplayName("Leveling up changes Zulaport Enforcer's power and toughness")
    void levelsUpAtThresholds() {
        Permanent enforcer = addCreatureReady(player1, new ZulaportEnforcer());

        assertThat(gqs.getEffectivePower(gd, enforcer)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, enforcer)).isEqualTo(1);

        prepareForLeveling(player1);
        levelUp(player1);

        assertThat(enforcer.getCounterCount(CounterType.LEVEL)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, enforcer)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, enforcer)).isEqualTo(3);

        levelUp(player1);
        assertThat(gqs.getEffectivePower(gd, enforcer)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, enforcer)).isEqualTo(3);

        levelUp(player1);
        assertThat(enforcer.getCounterCount(CounterType.LEVEL)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, enforcer)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, enforcer)).isEqualTo(5);
    }

    @Test
    @DisplayName("At level 3, Zulaport Enforcer can be blocked only by black creatures")
    void levelThreeCanOnlyBeBlockedByBlackCreatures() {
        Permanent enforcer = addCreatureReady(player1, new ZulaportEnforcer());
        prepareForLeveling(player1);
        levelUp(player1);
        levelUp(player1);
        levelUp(player1);

        Permanent blocker = addCreatureReady(player2, new NestInvader());
        declareAttackAndPrepareBlockers(enforcer);
        final int invalidBlockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        final int invalidAttackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(enforcer);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(invalidBlockerIndex, invalidAttackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("black creatures");

        gd.playerBattlefields.get(player2.getId()).clear();
        Permanent blackBlocker = addCreatureReady(player2, new ZulaportEnforcer());
        declareAttackAndPrepareBlockers(enforcer);
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blackBlocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(enforcer);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(blackBlocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Level up can only be activated at sorcery speed")
    void levelUpRequiresSorcerySpeed() {
        Permanent enforcer = addCreatureReady(player1, new ZulaportEnforcer());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> levelUp(player1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");

        assertThat(enforcer.getCounterCount(CounterType.LEVEL)).isZero();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2})
    @DisplayName("Below level 3, nonblack creatures can block Zulaport Enforcer")
    void nonblackCreaturesCanBlockBelowLevelThree(int level) {
        Permanent enforcer = addCreatureReady(player1, new ZulaportEnforcer());
        enforcer.setCounterCount(CounterType.LEVEL, level);
        Permanent blocker = addCreatureReady(player2, new NestInvader());
        declareAttackAndPrepareBlockers(enforcer);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Level up uses the stack and cannot be activated in response to itself")
    void levelUpRequiresAnEmptyStackAndAddsCounterOnResolution() {
        Permanent enforcer = addCreatureReady(player1, new ZulaportEnforcer());
        prepareForLeveling(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(enforcer.getCounterCount(CounterType.LEVEL)).isZero();
        assertThat(gd.stack).hasSize(1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");

        harness.passBothPriorities();

        assertThat(enforcer.getCounterCount(CounterType.LEVEL)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, enforcer)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, enforcer)).isEqualTo(3);
    }

    @Test
    @DisplayName("A tapped, summoning-sick Enforcer can level up")
    void levelUpDoesNotRequireTappingOrHaste() {
        Permanent enforcer = addCreatureReady(player1, new ZulaportEnforcer());
        enforcer.setSummoningSick(true);
        enforcer.tap();
        prepareForLeveling(player1);

        levelUp(player1);

        assertThat(enforcer.getCounterCount(CounterType.LEVEL)).isEqualTo(1);
        assertThat(enforcer.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Level up cannot be activated with only three mana")
    void levelUpRequiresFourMana() {
        Permanent enforcer = addCreatureReady(player1, new ZulaportEnforcer());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(enforcer.getCounterCount(CounterType.LEVEL)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Level up cannot be activated during the opponent's main phase")
    void levelUpRequiresYourOwnTurn() {
        Permanent enforcer = addCreatureReady(player1, new ZulaportEnforcer());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.ensurePriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");

        assertThat(enforcer.getCounterCount(CounterType.LEVEL)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Above level 3, nonblack creatures still cannot block")
    void topBandBlockingRestrictionHasNoUpperLimit() {
        Permanent enforcer = addCreatureReady(player1, new ZulaportEnforcer());
        enforcer.setCounterCount(CounterType.LEVEL, 4);
        addCreatureReady(player2, new NestInvader());
        declareAttackAndPrepareBlockers(enforcer);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("black creatures");
    }

    @Test
    @DisplayName("Level 4 retains the top band, and losing counters restores the lower bands")
    void levelBandsTrackCurrentCounterCount() {
        Permanent enforcer = addCreatureReady(player1, new ZulaportEnforcer());
        enforcer.setCounterCount(CounterType.LEVEL, 4);

        assertThat(gqs.getEffectivePower(gd, enforcer)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, enforcer)).isEqualTo(5);

        enforcer.setCounterCount(CounterType.LEVEL, 2);

        assertThat(gqs.getEffectivePower(gd, enforcer)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, enforcer)).isEqualTo(3);

        enforcer.setCounterCount(CounterType.LEVEL, 0);

        assertThat(gqs.getEffectivePower(gd, enforcer)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, enforcer)).isEqualTo(1);

        enforcer.setCounterCount(CounterType.LEVEL, 2);
        Permanent blocker = addCreatureReady(player2, new NestInvader());
        declareAttackAndPrepareBlockers(enforcer);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        assertThat(blocker.isBlocking()).isTrue();
    }

    private void prepareForLeveling(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player, ManaColor.COLORLESS, 12);
    }

    private void levelUp(Player player) {
        harness.activateAbility(player, 0, 0, null, null);
        harness.passBothPriorities();
    }

    private void declareAttackAndPrepareBlockers(Permanent enforcer) {
        enforcer.setAttacking(true);
        prepareDeclareBlockers();
    }
}
