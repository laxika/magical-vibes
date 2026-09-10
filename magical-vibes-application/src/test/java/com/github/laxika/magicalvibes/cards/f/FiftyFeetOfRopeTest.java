package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WallOfOmens;
import com.github.laxika.magicalvibes.model.Dungeon;
import com.github.laxika.magicalvibes.model.DungeonProgress;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FiftyFeetOfRope.class, GrizzlyBears.class, WallOfOmens.class})
class FiftyFeetOfRopeTest extends BaseCardTest {

    @Test
    @DisplayName("Climb Over prevents a Wall from blocking this turn")
    void climbOverPreventsWallFromBlocking() {
        harness.addToBattlefieldAndReturn(player1, new FiftyFeetOfRope());
        Permanent wall = addCreatureReady(player2, new WallOfOmens());

        harness.activateAbility(player1, 0, 0, null, wall.getId());
        harness.passBothPriorities();

        assertThat(wall.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Climb Over can target only Walls")
    void climbOverCannotTargetNonWall() {
        harness.addToBattlefieldAndReturn(player1, new FiftyFeetOfRope());
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Wall");
    }

    @Test
    @DisplayName("Tie Up keeps the target creature tapped through its next untap step")
    void tieUpSkipsTargetCreaturesNextUntap() {
        harness.addToBattlefieldAndReturn(player1, new FiftyFeetOfRope());
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        creature.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, creature.getId());
        harness.passBothPriorities();

        advanceToUpkeep(player2);

        assertThat(creature.isTapped()).isTrue();
        assertThat(creature.getSkipUntapCount()).isZero();
    }

    @Test
    @DisplayName("Rappel Down makes its controller venture into the dungeon")
    void rappelDownVentureIntoDungeon() {
        Permanent rope = harness.addToBattlefieldAndReturn(player1, new FiftyFeetOfRope());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(rope.isTapped()).isTrue();
        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 0));
    }

    @Test
    @DisplayName("Rappel Down requires sorcery timing")
    void rappelDownRequiresSorceryTiming() {
        harness.addToBattlefieldAndReturn(player1, new FiftyFeetOfRope());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("main phase");
    }
}
