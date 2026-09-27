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
        creeper.setAttacking(true);

        prepareDeclareBlockers();
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(creeper);

        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
