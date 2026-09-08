package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TreetopScout.class, AirElemental.class, GrizzlyBears.class})
class TreetopScoutTest extends BaseCardTest {

    @Test
    @DisplayName("Treetop Scout cannot be blocked by a creature without flying")
    void cannotBeBlockedByCreatureWithoutFlying() {
        Permanent scout = attackingScout();
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only be blocked by creatures with flying");
    }

    @Test
    @DisplayName("Treetop Scout can be blocked by a creature with flying")
    void canBeBlockedByCreatureWithFlying() {
        Permanent scout = attackingScout();
        Permanent blocker = addCreatureReady(player2, new AirElemental());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    private Permanent attackingScout() {
        Permanent scout = addCreatureReady(player1, new TreetopScout());
        scout.setAttacking(true);
        return scout;
    }
}
