package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.h.HillGiantHerdgorger;
import com.github.laxika.magicalvibes.cards.s.SecretDoor;
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

@CardUsed({FiftyFeetOfRope.class, HillGiantHerdgorger.class, SecretDoor.class})
class FiftyFeetOfRopeTest extends BaseCardTest {

    @Test
    @DisplayName("Climb Over prevents a Wall from blocking this turn")
    void climbOverPreventsWallFromBlocking() {
        harness.addToBattlefieldAndReturn(player1, new FiftyFeetOfRope());
        Permanent wall = addCreatureReady(player2, new SecretDoor());

        harness.activateAbility(player1, 0, 0, null, wall.getId());
        harness.passBothPriorities();

        assertThat(wall.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Climb Over can target only Walls")
    void climbOverCannotTargetNonWall() {
        harness.addToBattlefieldAndReturn(player1, new FiftyFeetOfRope());
        Permanent creature = addCreatureReady(player2, new HillGiantHerdgorger());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Wall");
    }

    @Test
    @DisplayName("Tie Up keeps the target creature tapped through its next untap step")
    void tieUpSkipsTargetCreaturesNextUntap() {
        harness.addToBattlefieldAndReturn(player1, new FiftyFeetOfRope());
        Permanent creature = addCreatureReady(player2, new HillGiantHerdgorger());
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

        harness.handleListChoice(player1, "Lost Mine of Phandelver");

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

    @Test
    void tieUpDoesNotTapAnUntappedCreatureAndExpiresAtNextUntap() {
        harness.addToBattlefield(player1, new FiftyFeetOfRope());
        Permanent creature = addCreatureReady(player2, new HillGiantHerdgorger());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isFalse();
        harness.performUntapStep(player2);
        creature.tap();
        harness.performUntapStep(player2);
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    void tieUpDoesNotExpireDuringOtherPlayersUntap() {
        harness.addToBattlefield(player1, new FiftyFeetOfRope());
        Permanent creature = addCreatureReady(player2, new HillGiantHerdgorger());
        creature.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, creature.getId());
        harness.passBothPriorities();

        harness.performUntapStep(player1);
        harness.performUntapStep(player2);
        assertThat(creature.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    void tieUpCannotTargetNoncreatureArtifact() {
        harness.addToBattlefield(player1, new FiftyFeetOfRope());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FiftyFeetOfRope());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rappelDownCanChooseAnotherDungeon() {
        harness.addToBattlefield(player1, new FiftyFeetOfRope());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Dungeon of the Mad Mage");
        resolveAllTriggers();

        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.DUNGEON_OF_THE_MAD_MAGE, 0));
        assertThat(gd.playerDungeonProgress.get(player2.getId())).isNull();
        harness.assertLife(player1, 21);
    }

    @Test
    void rappelDownAdvancesExistingDungeonAlongChosenPath() {
        harness.addToBattlefield(player1, new FiftyFeetOfRope());
        gd.playerDungeonProgress.put(player1.getId(), new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 0));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Goblin Lair");
        resolveAllTriggers();

        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 1));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Goblin") && permanent.getCard().isToken());
    }

    @Test
    void rappelDownCannotBeActivatedInResponse() {
        harness.addToBattlefield(player1, new FiftyFeetOfRope());
        harness.addToBattlefield(player1, new FiftyFeetOfRope());
        Permanent wall = addCreatureReady(player2, new SecretDoor());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, 0, null, wall.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 2, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");
    }
}
