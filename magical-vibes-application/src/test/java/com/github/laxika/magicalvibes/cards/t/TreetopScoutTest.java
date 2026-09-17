package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DawnElemental;
import com.github.laxika.magicalvibes.cards.s.ScornfulEgotist;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TreetopScout.class, DawnElemental.class, ScornfulEgotist.class})
class TreetopScoutTest extends BaseCardTest {

    @Test
    @DisplayName("Treetop Scout cannot be blocked by a creature without flying")
    void cannotBeBlockedByCreatureWithoutFlying() {
        addCreatureReady(player1, new TreetopScout());
        addCreatureReady(player2, new ScornfulEgotist());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only be blocked by creatures with flying");
    }

    @Test
    @DisplayName("Treetop Scout can be blocked by a creature with flying")
    void canBeBlockedByCreatureWithFlying() {
        addCreatureReady(player1, new TreetopScout());
        Permanent blocker = addCreatureReady(player2, new DawnElemental());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
