package com.github.laxika.magicalvibes.cards.c;

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

@CardUsed({CarnivorousPlant.class, GrizzlyBears.class})
class CarnivorousPlantTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot attack because it has defender")
    void cannotAttackBecauseOfDefender() {
        addCreatureReady(player1, new CarnivorousPlant());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Defender does not prevent blocking")
    void canBlockDespiteDefender() {
        addCreatureReady(player1, new GrizzlyBears());
        Permanent plant = addCreatureReady(player2, new CarnivorousPlant());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(plant.isBlocking()).isTrue();
    }
}
