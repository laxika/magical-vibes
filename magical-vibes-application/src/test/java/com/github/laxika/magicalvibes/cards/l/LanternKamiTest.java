package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.i.IsamaruHoundOfKonda;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LanternKami.class, IsamaruHoundOfKonda.class})
class LanternKamiTest extends BaseCardTest {

    @Test
    @DisplayName("Lantern Kami cannot be blocked by a creature without flying")
    void cannotBeBlockedByNonFlyingCreature() {
        addCreatureReady(player1, new LanternKami());
        addCreatureReady(player2, new IsamaruHoundOfKonda());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(
                gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot block Lantern Kami (flying)");
    }

    @Test
    @DisplayName("Lantern Kami can be blocked by a creature with flying")
    void canBeBlockedByFlyingCreature() {
        addCreatureReady(player1, new LanternKami());
        Permanent blocker = addCreatureReady(player2, new LanternKami());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
