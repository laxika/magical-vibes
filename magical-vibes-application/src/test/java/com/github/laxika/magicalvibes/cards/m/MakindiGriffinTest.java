package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SporecapSpider;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MakindiGriffin.class, GrizzlyBears.class, SporecapSpider.class})
class MakindiGriffinTest extends BaseCardTest {

    @Test
    @DisplayName("Makindi Griffin cannot be blocked by a creature without flying")
    void cannotBeBlockedByCreatureWithoutFlying() {
        Permanent griffin = addCreatureReady(player1, new MakindiGriffin());
        griffin.setAttacking(true);

        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot block Makindi Griffin (flying)");
    }

    @Test
    @DisplayName("Makindi Griffin can be blocked by a creature with flying")
    void canBeBlockedByCreatureWithFlying() {
        addCreatureReady(player1, new MakindiGriffin());
        Permanent blocker = addCreatureReady(player2, new MakindiGriffin());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Makindi Griffin can be blocked by a creature with reach")
    void canBeBlockedByCreatureWithReach() {
        addCreatureReady(player1, new MakindiGriffin());
        Permanent blocker = addCreatureReady(player2, new SporecapSpider());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
