package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TranscendentMaster.class})
class TranscendentMasterTest extends BaseCardTest {

    @Test
    @DisplayName("Leveling up changes Transcendent Master's stats and grants indestructible at level 12")
    void levelsUpAtThresholds() {
        Permanent master = addCreatureReady(player1, new TranscendentMaster());

        assertStats(master, 3, 3);
        assertThat(gqs.hasKeyword(gd, master, Keyword.INDESTRUCTIBLE)).isFalse();

        prepareForLeveling(player1);
        for (int i = 0; i < 6; i++) {
            levelUp(player1);
        }

        assertThat(master.getCounterCount(CounterType.LEVEL)).isEqualTo(6);
        assertStats(master, 6, 6);
        assertThat(gqs.hasKeyword(gd, master, Keyword.INDESTRUCTIBLE)).isFalse();

        for (int i = 0; i < 6; i++) {
            levelUp(player1);
        }

        assertThat(master.getCounterCount(CounterType.LEVEL)).isEqualTo(12);
        assertStats(master, 9, 9);
        assertThat(gqs.hasKeyword(gd, master, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Stats and keywords track level boundaries, including when counters are removed")
    void abilitiesTrackLevelBoundaries() {
        Permanent master = addCreatureReady(player1, new TranscendentMaster());
        for (int level : new int[]{0, 5, 6, 11, 12, 13, 11, 5, 0}) {
            master.setCounterCount(CounterType.LEVEL, level);
            assertStats(master, level >= 12 ? 9 : level >= 6 ? 6 : 3,
                    level >= 12 ? 9 : level >= 6 ? 6 : 3);
            assertThat(gqs.hasKeyword(gd, master, Keyword.LIFELINK)).isEqualTo(level >= 6);
            assertThat(gqs.hasKeyword(gd, master, Keyword.INDESTRUCTIBLE)).isEqualTo(level >= 12);
        }
    }

    @Test
    @DisplayName("Below level six combat damage does not gain life")
    void combatDamageWithoutLifelink() {
        assertUnblockedCombat(5, 3, 20);
    }

    @Test
    @DisplayName("At level six combat damage gains six life")
    void combatDamageWithLifelinkAtLevelSix() {
        assertUnblockedCombat(6, 6, 26);
    }

    @Test
    @DisplayName("At level twelve combat damage gains nine life")
    void combatDamageWithLifelinkAtLevelTwelve() {
        assertUnblockedCombat(12, 9, 29);
    }

    @Test
    @DisplayName("Level up cannot be activated outside a main phase")
    void levelUpRequiresMainPhase() {
        addCreatureReady(player1, new TranscendentMaster());
        prepareForLeveling(player1);
        harness.forceStep(TurnStep.UPKEEP);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Level up cannot be activated during an opponent's turn")
    void levelUpRequiresOwnTurn() {
        addCreatureReady(player1, new TranscendentMaster());
        prepareForLeveling(player1);
        harness.forceActivePlayer(player2);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Level up uses the stack and requires an empty stack")
    void levelUpRequiresEmptyStack() {
        Permanent master = addCreatureReady(player1, new TranscendentMaster());
        prepareForLeveling(player1);
        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(master.getCounterCount(CounterType.LEVEL)).isZero();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.passBothPriorities();
        assertThat(master.getCounterCount(CounterType.LEVEL)).isEqualTo(1);
    }

    @Test
    @DisplayName("Level up remains available above the highest level threshold")
    void canLevelUpBeyondHighestThreshold() {
        Permanent master = addCreatureReady(player1, new TranscendentMaster());
        master.setCounterCount(CounterType.LEVEL, 12);
        prepareForLeveling(player1);
        levelUp(player1);
        assertThat(master.getCounterCount(CounterType.LEVEL)).isEqualTo(13);
        assertStats(master, 9, 9);
        assertThat(gqs.hasKeyword(gd, master, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, master, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Level twelve Masters survive lethal combat damage")
    void indestructiblePreventsLethalCombatDamage() {
        Permanent attacker = addCreatureReady(player1, new TranscendentMaster());
        Permanent blocker = addCreatureReady(player2, new TranscendentMaster());
        attacker.setCounterCount(CounterType.LEVEL, 12);
        blocker.setCounterCount(CounterType.LEVEL, 12);
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
        harness.assertLife(player1, 29);
        harness.assertLife(player2, 29);
    }

    @Test
    @DisplayName("Below level twelve Masters die to lethal combat damage")
    void lacksIndestructibleBelowLevelTwelve() {
        Permanent attacker = addCreatureReady(player1, new TranscendentMaster());
        Permanent blocker = addCreatureReady(player2, new TranscendentMaster());
        attacker.setCounterCount(CounterType.LEVEL, 11);
        blocker.setCounterCount(CounterType.LEVEL, 11);
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        harness.assertLife(player1, 26);
        harness.assertLife(player2, 26);
    }

    private void assertUnblockedCombat(int level, int damage, int controllerLife) {
        Permanent master = addCreatureReady(player1, new TranscendentMaster());
        master.setCounterCount(CounterType.LEVEL, level);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        declareAttackers(List.of(0));
        resolveCombat();
        harness.assertLife(player1, controllerLife);
        harness.assertLife(player2, 20 - damage);
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

    private void assertStats(Permanent permanent, int power, int toughness) {
        assertThat(gqs.getEffectivePower(gd, permanent)).isEqualTo(power);
        assertThat(gqs.getEffectiveToughness(gd, permanent)).isEqualTo(toughness);
    }
}
