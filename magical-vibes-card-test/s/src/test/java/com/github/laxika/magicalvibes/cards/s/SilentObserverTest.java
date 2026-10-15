package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DevilthornFox;
import com.github.laxika.magicalvibes.cards.w.WatcherInTheWeb;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;

@CardUsed({SilentObserver.class, DevilthornFox.class, WatcherInTheWeb.class})
class SilentObserverTest extends BaseCardTest {

    @Test
    @DisplayName("A creature without flying or reach cannot block Silent Observer")
    void nonFlyingCreatureCannotBlockSilentObserver() {
        Permanent observer = addCreatureReady(player1, new SilentObserver());
        observer.setAttacking(true);
        addCreatureReady(player2, new DevilthornFox());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    @DisplayName("A creature with flying can block Silent Observer")
    void flyingCreatureCanBlockSilentObserver() {
        Permanent observer = addCreatureReady(player1, new SilentObserver());
        observer.setAttacking(true);
        addCreatureReady(player2, new SilentObserver());
        prepareDeclareBlockers();

        assertThatCode(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0)))).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("A creature with reach can block Silent Observer")
    void reachCreatureCanBlockSilentObserver() {
        Permanent observer = addCreatureReady(player1, new SilentObserver());
        observer.setAttacking(true);
        addCreatureReady(player2, new WatcherInTheWeb());
        prepareDeclareBlockers();

        assertThatCode(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0)))).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Silent Observer can block a creature without flying")
    void silentObserverCanBlockGroundCreature() {
        Permanent fox = addCreatureReady(player1, new DevilthornFox());
        fox.setAttacking(true);
        addCreatureReady(player2, new SilentObserver());
        prepareDeclareBlockers();

        assertThatCode(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0)))).doesNotThrowAnyException();
    }
}
