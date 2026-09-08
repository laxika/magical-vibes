package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AlabasterDragon;
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

@CardUsed({CloudPirates.class, AlabasterDragon.class, GrizzlyBears.class})
class CloudPiratesTest extends BaseCardTest {

    @Test
    @DisplayName("Cloud Pirates can block a creature with flying")
    void canBlockFlyingCreature() {
        Permanent pirates = addCreatureReady(player2, new CloudPirates());

        addCreatureReady(player1, new AlabasterDragon());

        declareAttackers(player1, List.of(0));
        prepareDeclareBlockers(player1);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(pirates.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Cloud Pirates cannot block a creature without flying")
    void cannotBlockNonFlyingCreature() {
        addCreatureReady(player2, new CloudPirates());

        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only block creatures with flying");
    }

    @Test
    @DisplayName("Cloud Pirates cannot be blocked by a creature without flying")
    void flyingPreventsNonFlyingBlocker() {
        addCreatureReady(player1, new CloudPirates());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("(flying)");
    }
}
