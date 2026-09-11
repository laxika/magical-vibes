package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.ArgothianSwine;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.z.Zephid;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TreetopRangers.class, ArgothianSwine.class, Zephid.class})
class TreetopRangersTest extends BaseCardTest {

    @Test
    @DisplayName("Treetop Rangers cannot be blocked by a creature without flying")
    void cannotBeBlockedByNonFlyingCreature() {
        addAttackingTreetopRangers();

        addCreatureReady(player2, new ArgothianSwine());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only be blocked by creatures with flying");
    }

    @Test
    @DisplayName("Treetop Rangers can be blocked by a creature with flying")
    void canBeBlockedByFlyingCreature() {
        addAttackingTreetopRangers();

        Permanent blocker = addCreatureReady(player2, new Zephid());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @CardUsed(GiantSpider.class)
    @DisplayName("Treetop Rangers cannot be blocked by a creature with reach but not flying")
    void reachDoesNotSatisfyFlyingRestriction() {
        addAttackingTreetopRangers();

        addCreatureReady(player2, new GiantSpider());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only be blocked by creatures with flying");
    }

    private void addAttackingTreetopRangers() {
        Permanent attacker = addCreatureReady(player1, new TreetopRangers());
        attacker.setAttacking(true);
    }
}
