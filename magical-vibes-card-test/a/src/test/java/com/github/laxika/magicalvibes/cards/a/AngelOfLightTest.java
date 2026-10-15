package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AngelOfLight.class, GrizzlyBears.class})
class AngelOfLightTest extends BaseCardTest {

    @Test
    void flyingPreventsGroundCreatureFromBlocking() {
        addCreatureReady(player1, new AngelOfLight());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(player1, List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot block");
    }

    @Test
    void vigilanceKeepsAngelOfLightUntappedWhenItAttacks() {
        Permanent angel = addCreatureReady(player1, new AngelOfLight());

        declareAttackers(player1, List.of(0));

        assertThat(angel.isTapped()).isFalse();
    }

    @Test
    void flyingCreatureCanBlockAngelOfLight() {
        addCreatureReady(player1, new AngelOfLight());
        Permanent blocker = addCreatureReady(player2, new AngelOfLight());

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void vigilanceDoesNotAllowTappedAngelToAttack() {
        Permanent angel = addCreatureReady(player1, new AngelOfLight());
        angel.tap();

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");

        assertThat(angel.isAttacking()).isFalse();
        assertThat(angel.isTapped()).isTrue();
    }
}
