package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AlabornTrooper;
import com.github.laxika.magicalvibes.cards.n.NorwoodArchers;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WildGriffin.class, AlabornTrooper.class, NorwoodArchers.class})
class WildGriffinTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot be blocked by a creature without flying or reach")
    void cannotBeBlockedByCreatureWithoutFlyingOrReach() {
        addCreatureReady(player1, new WildGriffin());
        addCreatureReady(player2, new AlabornTrooper());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot block Wild Griffin (flying)");
    }

    @Test
    @DisplayName("Can block a creature without flying")
    void canBlockCreatureWithoutFlying() {
        addCreatureReady(player1, new AlabornTrooper());
        Permanent griffin = addCreatureReady(player2, new WildGriffin());

        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(griffin.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Can be blocked by another creature with flying")
    void canBeBlockedByCreatureWithFlying() {
        addCreatureReady(player1, new WildGriffin());
        Permanent blocker = addCreatureReady(player2, new WildGriffin());

        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Can be blocked by a creature with reach")
    void canBeBlockedByCreatureWithReach() {
        addCreatureReady(player1, new WildGriffin());
        Permanent blocker = addCreatureReady(player2, new NorwoodArchers());

        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
