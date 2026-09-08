package com.github.laxika.magicalvibes.cards.w;

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

@CardUsed({WallOfAir.class, AirElemental.class, GrizzlyBears.class})
class WallOfAirTest extends BaseCardTest {

    @Test
    @DisplayName("Defender prevents Wall of Air from attacking")
    void cannotAttackBecauseOfDefender() {
        addCreatureReady(player1, new WallOfAir());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Wall of Air can block a creature with flying")
    void canBlockFlyingCreature() {
        addCreatureReady(player1, new AirElemental());
        Permanent wall = addCreatureReady(player2, new WallOfAir());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(wall.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Wall of Air can block a creature without flying")
    void canBlockNonFlyingCreature() {
        addCreatureReady(player1, new GrizzlyBears());
        Permanent wall = addCreatureReady(player2, new WallOfAir());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(wall.isBlocking()).isTrue();
    }
}
