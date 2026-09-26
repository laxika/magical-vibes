package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.r.RagingBull;
import com.github.laxika.magicalvibes.cards.w.WallOfEarth;
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

@CardUsed({GlyphOfDelusion.class, WallOfEarth.class, RagingBull.class})
class GlyphOfDelusionTest extends BaseCardTest {

    @Test
    void putsCountersBasedOnBlockedCreaturesPowerAndGrantsTheTwoAbilities() {
        Permanent attacker = addCreatureReady(player1, new RagingBull());
        Permanent wall = addCreatureReady(player2, new WallOfEarth());
        declareAttackersAndPrepareBlockers(player1, List.of(0));
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
    void unlocksAfterTheLastGlyphCounterIsRemoved() {
        Permanent attacker = addCreatureReady(player1, new RagingBull());
        Permanent wall = addCreatureReady(player2, new WallOfEarth());
        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        castGlyph(wall, attacker);
        attacker.tap();

        advanceToUpkeep(player1);
        resolveAllTriggers();
        assertThat(attacker.getCounterCount(CounterType.GLYPH)).isEqualTo(1);
        assertThat(attacker.isTapped()).isTrue();

        advanceToUpkeep(player1);
        resolveAllTriggers();
        assertThat(attacker.getCounterCount(CounterType.GLYPH)).isZero();
        assertThat(attacker.isTapped()).isTrue();

        advanceToUpkeep(player1);
        assertThat(attacker.isTapped()).isFalse();
        resolveAllTriggers();
    }

    @Test
    void cannotTargetACreatureNotBlockedByTheWall() {
        Permanent blockedAttacker = addCreatureReady(player1, new RagingBull());
        Permanent unblockedAttacker = addCreatureReady(player1, new RagingBull());
        Permanent wall = addCreatureReady(player2, new WallOfEarth());
        declareAttackersAndPrepareBlockers(player1, List.of(0, 1));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.setHand(player1, List.of(new GlyphOfDelusion()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(wall.getId(), unblockedAttacker.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("blocked by the first target");
        assertThat(blockedAttacker.getCounterCount(CounterType.GLYPH)).isZero();
    }

    @Test
    void cannotTargetANonWallAsTheBlocker() {
        Permanent attacker = addCreatureReady(player1, new RagingBull());
        Permanent nonWallBlocker = addCreatureReady(player2, new RagingBull());
        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.setHand(player1, List.of(new GlyphOfDelusion()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(nonWallBlocker.getId(), attacker.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void eachGlyphResolutionAddsAnotherUpkeepTrigger() {
        Permanent attacker = addCreatureReady(player1, new RagingBull());
        Permanent wall = addCreatureReady(player2, new WallOfEarth());
        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.setHand(player1, List.of(new GlyphOfDelusion(), new GlyphOfDelusion()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, List.of(wall.getId(), attacker.getId()));
        harness.passBothPriorities();
        harness.castInstant(player1, 0, List.of(wall.getId(), attacker.getId()));
        harness.passBothPriorities();

        assertThat(attacker.getCounterCount(CounterType.GLYPH)).isEqualTo(4);
        attacker.tap();

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(attacker.getCounterCount(CounterType.GLYPH)).isEqualTo(2);
        assertThat(attacker.isTapped()).isTrue();
    }

    private void castGlyph(Permanent wall, Permanent creature) {
        harness.setHand(player1, List.of(new GlyphOfDelusion()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, List.of(wall.getId(), creature.getId()));
        harness.passBothPriorities();
    }
}
