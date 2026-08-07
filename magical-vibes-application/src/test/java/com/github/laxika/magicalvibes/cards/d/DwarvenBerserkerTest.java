package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DwarvenBerserkerTest extends BaseCardTest {

    @Test
    @DisplayName("Becoming blocked gives +3/+0 and trample")
    void becomingBlockedPumpsAndGrantsTrample() {
        Permanent berserker = addReadyBerserker(player1);
        berserker.setAttacking(true);
        addReadyBears(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(berserker.getPowerModifier()).isEqualTo(3);
        assertThat(berserker.getToughnessModifier()).isZero();
        assertThat(berserker.getEffectivePower()).isEqualTo(4);
        assertThat(berserker.hasKeyword(Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Multiple blockers still give only +3/+0 once")
    void multipleBlockersTriggerOnce() {
        Permanent berserker = addReadyBerserker(player1);
        berserker.setAttacking(true);
        addReadyBears(player2);
        addReadyBears(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(berserker.getPowerModifier()).isEqualTo(3);
        assertThat(berserker.hasKeyword(Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("If unblocked nothing triggers")
    void unblockedCreatesNoTrigger() {
        Permanent berserker = addReadyBerserker(player1);
        berserker.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(berserker.getPowerModifier()).isZero();
        assertThat(berserker.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    private Permanent addReadyBerserker(Player player) {
        Permanent permanent = new Permanent(new DwarvenBerserker());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void addReadyBears(Player player) {
        Permanent permanent = new Permanent(new GrizzlyBears());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
    }
}
