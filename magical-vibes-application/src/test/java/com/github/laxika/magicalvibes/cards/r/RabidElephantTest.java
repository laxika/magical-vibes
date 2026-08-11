package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RabidElephantTest extends BaseCardTest {

    @Test
    @DisplayName("With one blocker Rabid Elephant gets +2/+2 until end of turn")
    void oneBlockerGivesPlusTwo() {
        Permanent elephant = addReadyElephant(player1);
        elephant.setAttacking(true);
        addReadyBears(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(elephant.getPowerModifier()).isEqualTo(2);
        assertThat(elephant.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("With two blockers Rabid Elephant gets +4/+4 until end of turn")
    void twoBlockersGivesPlusFour() {
        Permanent elephant = addReadyElephant(player1);
        elephant.setAttacking(true);
        addReadyBears(player2);
        addReadyBears(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));
        harness.passBothPriorities();

        assertThat(elephant.getPowerModifier()).isEqualTo(4);
        assertThat(elephant.getToughnessModifier()).isEqualTo(4);
    }

    @Test
    @DisplayName("If unblocked no becomes-blocked trigger is created")
    void unblockedCreatesNoTrigger() {
        Permanent elephant = addReadyElephant(player1);
        elephant.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(elephant.getPowerModifier()).isZero();
        assertThat(elephant.getToughnessModifier()).isZero();
    }

    private Permanent addReadyElephant(Player player) {
        Permanent permanent = new Permanent(new RabidElephant());
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
