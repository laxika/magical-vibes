package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WallOfFire;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Dungeon;
import com.github.laxika.magicalvibes.model.DungeonProgress;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KickInTheDoor.class, GrizzlyBears.class, WallOfFire.class, FountainOfYouth.class})
class KickInTheDoorTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a counter and grants haste, Wall evasion, and venture")
    void appliesAllEffects() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new KickInTheDoor()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, bears.getId());
        harness.handleListChoice(player1, "Lost Mine of Phandelver");

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(bears.getGrantedKeywords()).contains(Keyword.HASTE);
        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 0));
    }

    @Test
    @DisplayName("Cannot be blocked by Walls this turn")
    void cannotBeBlockedByWall() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent wall = addCreatureReady(player2, new WallOfFire());
        castKickInTheDoor(attacker);

        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        int wallIndex = gd.playerBattlefields.get(player2.getId()).indexOf(wall);
        declareAttackersAndPrepareBlockers(player1, List.of(attackerIndex));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(wallIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only be blocked by non-Wall creatures");
    }

    @Test
    @DisplayName("Can be blocked by a non-Wall creature this turn")
    void canBeBlockedByNonWall() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        castKickInTheDoor(attacker);

        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        declareAttackersAndPrepareBlockers(player1, List.of(attackerIndex));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Wall restriction wears off at cleanup")
    void wallRestrictionWearsOffAtCleanup() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent wall = addCreatureReady(player2, new WallOfFire());
        castKickInTheDoor(attacker);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(attacker.getGrantedKeywords()).doesNotContain(Keyword.HASTE);
        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        int wallIndex = gd.playerBattlefields.get(player2.getId()).indexOf(wall);
        declareAttackersAndPrepareBlockers(player1, List.of(attackerIndex));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(wallIndex, attackerIndex)));

        assertThat(wall.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent fountain = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new KickInTheDoor()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, fountain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Can target an opponent's creature while the caster ventures")
    void targetsOpponentsCreature() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        castKickInTheDoor(target);

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(target.getGrantedKeywords()).contains(Keyword.HASTE);
        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 0));
        assertThat(gd.playerDungeonProgress).doesNotContainKey(player2.getId());
    }

    @Test
    @DisplayName("Does not venture when its only target leaves before resolution")
    void doesNotVentureWithMissingTarget() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new KickInTheDoor()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castSorcery(player1, 0, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerGraveyards.get(player1.getId()).add(target.getCard());

        harness.passBothPriorities();

        assertThat(gd.playerDungeonProgress).doesNotContainKey(player1.getId());
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Kick in the Door");
    }

    @Test
    @DisplayName("Allows the caster to choose a different dungeon")
    void choosesTombOfAnnihilation() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new KickInTheDoor()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveSorcery(player1, 0, target.getId());

        harness.handleListChoice(player1, "Tomb of Annihilation");
        resolveAllTriggers();

        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.TOMB_OF_ANNIHILATION, 0));
        harness.assertLife(player1, 19);
        harness.assertLife(player2, 19);
    }

    private void castKickInTheDoor(Permanent target) {
        harness.setHand(player1, List.of(new KickInTheDoor()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.castAndResolveSorcery(player1, 0, target.getId());
        harness.handleListChoice(player1, "Lost Mine of Phandelver");
        resolveAllTriggers();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
    }
}
