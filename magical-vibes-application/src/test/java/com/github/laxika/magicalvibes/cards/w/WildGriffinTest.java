package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AlabornTrooper;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WildGriffin.class, AlabornTrooper.class})
class WildGriffinTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot be blocked by a creature without flying or reach")
    void cannotBeBlockedByCreatureWithoutFlyingOrReach() {
        Permanent griffin = addCreatureReady(player1, new WildGriffin());
        griffin.setAttacking(true);
        addCreatureReady(player2, new AlabornTrooper());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot block Wild Griffin (flying)");
    }

    @Test
    @DisplayName("Can block a creature without flying")
    void canBlockCreatureWithoutFlying() {
        Permanent attacker = addCreatureReady(player1, new AlabornTrooper());
        attacker.setAttacking(true);
        Permanent griffin = addCreatureReady(player2, new WildGriffin());

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(griffin.isBlocking()).isTrue();
    }
}
