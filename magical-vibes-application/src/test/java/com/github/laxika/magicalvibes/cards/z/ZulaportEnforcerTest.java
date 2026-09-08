package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.d.DreadWarlock;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        declareAttackAndPrepareBlockers(enforcer);
        final int invalidBlockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        final int invalidAttackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(enforcer);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(invalidBlockerIndex, invalidAttackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("black creatures");

        gd.playerBattlefields.get(player2.getId()).clear();
        Permanent blackBlocker = addCreatureReady(player2, new DreadWarlock());
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
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }
}
