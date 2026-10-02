package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AssaultGriffin.class, RuneclawBear.class, GiantSpider.class})
class AssaultGriffinTest extends BaseCardTest {

    @Test
    void cannotBeBlockedByCreatureWithoutFlyingOrReach() {
        addCreatureReady(player1, new AssaultGriffin());
        addCreatureReady(player2, new RuneclawBear());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    void canBeBlockedByFlyingCreature() {
        addCreatureReady(player1, new AssaultGriffin());
        Permanent blocker = addCreatureReady(player2, new AssaultGriffin());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void canBeBlockedByCreatureWithReach() {
        addCreatureReady(player1, new AssaultGriffin());
        Permanent blocker = addCreatureReady(player2, new GiantSpider());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void canBlockCreatureWithoutFlying() {
        addCreatureReady(player1, new RuneclawBear());
        Permanent blocker = addCreatureReady(player2, new AssaultGriffin());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
