package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HalimarWavewatch.class, Island.class})
class HalimarWavewatchTest extends BaseCardTest {

    @Test
    @DisplayName("Leveling up changes Halimar Wavewatch's base power, toughness, and islandwalk")
    void levelsUpAtThresholds() {
        Permanent wavewatch = addCreatureReady(player1, new HalimarWavewatch());

        assertStats(wavewatch, 0, 3);
        assertThat(gqs.hasKeyword(gd, wavewatch, Keyword.ISLANDWALK)).isFalse();

        prepareForLeveling(player1);
        levelUp(player1);

        assertThat(wavewatch.getCounterCount(CounterType.LEVEL)).isEqualTo(1);
        assertStats(wavewatch, 0, 6);
        assertThat(gqs.hasKeyword(gd, wavewatch, Keyword.ISLANDWALK)).isFalse();

        for (int i = 0; i < 3; i++) {
            levelUp(player1);
        }

        assertThat(wavewatch.getCounterCount(CounterType.LEVEL)).isEqualTo(4);
        assertStats(wavewatch, 0, 6);
        assertThat(gqs.hasKeyword(gd, wavewatch, Keyword.ISLANDWALK)).isFalse();

        levelUp(player1);

        assertThat(wavewatch.getCounterCount(CounterType.LEVEL)).isEqualTo(5);
        assertStats(wavewatch, 6, 6);
        assertThat(gqs.hasKeyword(gd, wavewatch, Keyword.ISLANDWALK)).isTrue();
    }

    @Test
    @DisplayName("Level up can only be activated at sorcery speed")
    void levelUpRequiresSorcerySpeed() {
        Permanent wavewatch = addCreatureReady(player1, new HalimarWavewatch());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> levelUp(player1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");

        assertThat(wavewatch.getCounterCount(CounterType.LEVEL)).isZero();
    }

    @Test
    void levelCounterIsAddedOnResolutionAndAnotherActivationRequiresAnEmptyStack() {
        Permanent wavewatch = addCreatureReady(player1, new HalimarWavewatch());
        prepareForLeveling(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(wavewatch.getCounterCount(CounterType.LEVEL)).isZero();
        assertStats(wavewatch, 0, 3);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.passBothPriorities();

        assertThat(wavewatch.getCounterCount(CounterType.LEVEL)).isEqualTo(1);
        assertStats(wavewatch, 0, 6);
    }

    @Test
    void levelUpDoesNotRequireTappingOrHaste() {
        Permanent wavewatch = addCreatureReady(player1, new HalimarWavewatch());
        wavewatch.setSummoningSick(true);
        wavewatch.tap();
        prepareForLeveling(player1);

        levelUp(player1);

        assertThat(wavewatch.getCounterCount(CounterType.LEVEL)).isEqualTo(1);
        assertThat(wavewatch.isTapped()).isTrue();
    }

    @Test
    void levelUpCannotBeActivatedDuringTheOpponentsMainPhase() {
        Permanent wavewatch = addCreatureReady(player1, new HalimarWavewatch());
        prepareForLeveling(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
        assertThat(wavewatch.getCounterCount(CounterType.LEVEL)).isZero();
    }

    @Test
    void levelUpRequiresTwoMana() {
        Permanent wavewatch = addCreatureReady(player1, new HalimarWavewatch());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(wavewatch.getCounterCount(CounterType.LEVEL)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void decreasingLevelCountersRestoresTheLowerLevelCharacteristics() {
        Permanent wavewatch = addCreatureReady(player1, new HalimarWavewatch());
        wavewatch.setCounterCount(CounterType.LEVEL, 7);
        assertStats(wavewatch, 6, 6);
        assertThat(gqs.hasKeyword(gd, wavewatch, Keyword.ISLANDWALK)).isTrue();

        wavewatch.setCounterCount(CounterType.LEVEL, 4);
        assertStats(wavewatch, 0, 6);
        assertThat(gqs.hasKeyword(gd, wavewatch, Keyword.ISLANDWALK)).isFalse();

        wavewatch.setCounterCount(CounterType.LEVEL, 0);
        assertStats(wavewatch, 0, 3);
        assertThat(gqs.hasKeyword(gd, wavewatch, Keyword.ISLANDWALK)).isFalse();
    }

    @ParameterizedTest
    @CsvSource({"0, true, false", "4, true, false", "5, true, true", "7, true, true", "5, false, false"})
    void islandwalkPreventsBlockingOnlyAtTheTopLevelWithADefendingIsland(
            int level, boolean defendingIsland, boolean cannotBlock) {
        Permanent wavewatch = addCreatureReady(player1, new HalimarWavewatch());
        wavewatch.setCounterCount(CounterType.LEVEL, level);
        addCreatureReady(player2, new HalimarWavewatch());
        harness.addToBattlefield(defendingIsland ? player2 : player1, new Island());

        declareAttackersAndPrepareBlockers(List.of(0));

        if (cannotBlock) {
            assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                    .isInstanceOf(IllegalStateException.class);
        } else {
            gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        }
    }

    private void prepareForLeveling(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player, ManaColor.COLORLESS, 10);
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
