package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.FaerieMacabre;
import com.github.laxika.magicalvibes.cards.s.SafeholdSentry;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Gloomwidow.class, FaerieMacabre.class, SafeholdSentry.class})
class GloomwidowTest extends BaseCardTest {

    @Test
    @DisplayName("Gloomwidow can block a creature with flying")
    void canBlockFlyingCreature() {
        Permanent widowPerm = addCreatureReady(player2, new Gloomwidow());
        addCreatureReady(player1, new FaerieMacabre());

        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(widowPerm.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Gloomwidow cannot block a creature without flying")
    void cannotBlockNonFlyingCreature() {
        addCreatureReady(player2, new Gloomwidow());
        addCreatureReady(player1, new SafeholdSentry());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only block creatures with flying");
    }
}
