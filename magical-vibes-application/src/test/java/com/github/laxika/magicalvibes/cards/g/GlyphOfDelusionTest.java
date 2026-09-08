package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WallOfWood;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GlyphOfDelusion.class, WallOfWood.class, GrizzlyBears.class})
class GlyphOfDelusionTest extends BaseCardTest {

    @Test
    void putsCountersBasedOnBlockedCreaturesPowerAndGrantsTheTwoAbilities() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent wall = addCreatureReady(player2, new WallOfWood());
        declareAttackers(player1, List.of(0));
        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        castGlyph(wall, attacker);

        assertThat(attacker.getCounterCount(CounterType.GLYPH)).isEqualTo(2);
        attacker.tap();
        advanceToUpkeep(player1);
        assertThat(attacker.isTapped()).isTrue();
        harness.passBothPriorities();
        assertThat(attacker.getCounterCount(CounterType.GLYPH)).isEqualTo(1);
    }

    @Test
    void cannotTargetACreatureNotBlockedByTheWall() {
        Permanent blockedAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent unblockedAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent wall = addCreatureReady(player2, new WallOfWood());
        declareAttackers(player1, List.of(0, 1));
        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.setHand(player1, List.of(new GlyphOfDelusion()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(wall.getId(), unblockedAttacker.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("blocked by the first target");
        assertThat(blockedAttacker.getCounterCount(CounterType.GLYPH)).isZero();
    }

    private void castGlyph(Permanent wall, Permanent creature) {
        harness.setHand(player1, List.of(new GlyphOfDelusion()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, List.of(wall.getId(), creature.getId()));
        harness.passBothPriorities();
    }
}
