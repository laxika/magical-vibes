package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Hexdrinker.class, GrizzlyBears.class, Shock.class})
class HexdrinkerTest extends BaseCardTest {

    @Test
    @DisplayName("Leveling up changes Hexdrinker's stats and protection at each threshold")
    void levelsUpAtThresholds() {
        Permanent hexdrinker = addCreatureReady(player1, new Hexdrinker());

        assertStats(hexdrinker, 2, 1);

        prepareForLeveling(player1, 8);
        levelUp(player1, 3);

        assertThat(hexdrinker.getCounterCount(CounterType.LEVEL)).isEqualTo(3);
        assertStats(hexdrinker, 4, 4);

        levelUp(player1, 5);

        assertThat(hexdrinker.getCounterCount(CounterType.LEVEL)).isEqualTo(8);
        assertStats(hexdrinker, 6, 6);
    }

    @Test
    @DisplayName("At level 3, Hexdrinker has protection from instants")
    void levelThreeHasProtectionFromInstants() {
        Permanent hexdrinker = addCreatureReady(player1, new Hexdrinker());
        prepareForLeveling(player1, 3);
        levelUp(player1, 3);

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, hexdrinker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from instants");
    }

    @Test
    @DisplayName("At level 8, Hexdrinker cannot be blocked")
    void levelEightHasProtectionFromEverything() {
        Permanent hexdrinker = addCreatureReady(player1, new Hexdrinker());
        prepareForLeveling(player1, 8);
        levelUp(player1, 8);

        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        hexdrinker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(hexdrinker);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    private void prepareForLeveling(Player player, int mana) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player, ManaColor.COLORLESS, mana);
    }

    private void levelUp(Player player, int times) {
        for (int i = 0; i < times; i++) {
            harness.activateAbility(player, 0, 0, null, null);
            harness.passBothPriorities();
        }
    }

    private void assertStats(Permanent permanent, int power, int toughness) {
        assertThat(gqs.getEffectivePower(gd, permanent)).isEqualTo(power);
        assertThat(gqs.getEffectiveToughness(gd, permanent)).isEqualTo(toughness);
    }
}
