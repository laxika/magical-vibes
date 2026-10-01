package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.Drekavac;
import com.github.laxika.magicalvibes.cards.m.MistralCharger;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GnatAlleyCreeper.class, MistralCharger.class, Drekavac.class})
class GnatAlleyCreeperTest extends BaseCardTest {

    @Test
    @DisplayName("Gnat Alley Creeper can't be blocked by a creature with flying")
    void cannotBeBlockedByFlyingCreature() {
        addCreatureReady(player1, new GnatAlleyCreeper());
        addCreatureReady(player2, new MistralCharger());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Gnat Alley Creeper can be blocked by a creature without flying")
    void canBeBlockedByNonFlyingCreature() {
        addCreatureReady(player1, new GnatAlleyCreeper());
        Permanent blocker = addCreatureReady(player2, new Drekavac());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
