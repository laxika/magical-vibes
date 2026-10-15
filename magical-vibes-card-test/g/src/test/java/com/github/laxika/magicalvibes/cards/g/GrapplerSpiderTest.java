package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.h.HorizonDrake;
import com.github.laxika.magicalvibes.cards.l.LeatherbackBaloth;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GrapplerSpider.class, HorizonDrake.class, LeatherbackBaloth.class})
class GrapplerSpiderTest extends BaseCardTest {

    @Test
    @DisplayName("Grappler Spider can block a creature with flying")
    void canBlockFlyingCreature() {
        addCreatureReady(player2, new GrapplerSpider());
        addCreatureReady(player1, new HorizonDrake()).setAttacking(true);

        prepareDeclareBlockers();

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Grappler Spider can also block a non-flying creature")
    void canBlockNonFlyingCreature() {
        addCreatureReady(player2, new GrapplerSpider());
        addCreatureReady(player1, new LeatherbackBaloth()).setAttacking(true);

        prepareDeclareBlockers();

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Reach does not prevent a non-flying creature from blocking Grappler Spider")
    void reachDoesNotGrantFlying() {
        addCreatureReady(player2, new LeatherbackBaloth());
        addCreatureReady(player1, new GrapplerSpider()).setAttacking(true);

        prepareDeclareBlockers();

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("A tapped Grappler Spider cannot block a flying creature")
    void tappedSpiderCannotBlockFlyingCreature() {
        addCreatureReady(player2, new GrapplerSpider()).tap();
        addCreatureReady(player1, new HorizonDrake()).setAttacking(true);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }
}
