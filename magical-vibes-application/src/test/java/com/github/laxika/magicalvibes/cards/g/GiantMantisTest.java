package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BayFalcon;
import com.github.laxika.magicalvibes.cards.w.WildElephant;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GiantMantis.class, BayFalcon.class, WildElephant.class})
class GiantMantisTest extends BaseCardTest {

    @Test
    @DisplayName("Reach lets Giant Mantis block a creature with flying")
    void reachAllowsBlockingFlyingCreature() {
        addCreatureReady(player1, new BayFalcon());
        Permanent mantis = addCreatureReady(player2, new GiantMantis());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(mantis.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("A creature without reach cannot block a creature with flying")
    void creatureWithoutReachCannotBlockFlyingCreature() {
        addCreatureReady(player1, new BayFalcon());
        addCreatureReady(player2, new WildElephant());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }
}
