package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KnucklesTheEchidna.class, LeoninScimitar.class, GrizzlyBears.class})
class KnucklesTheEchidnaTest extends BaseCardTest {

    @Test
    @DisplayName("Creates one Treasure for each combat damage step that reaches a player")
    void createsTreasureForBothDoubleStrikeDamageSteps() {
        addCreatureReady(player1, new KnucklesTheEchidna());

        declareAttackersAndPrepareBlockers(List.of(0));
        resolveCombatUnblocked();

        assertThat(treasureCount(player1)).isEqualTo(2);
    }

    @Test
    @DisplayName("Wins the game at upkeep while controlling thirty artifacts")
    void winsWithThirtyArtifacts() {
        harness.addToBattlefield(player1, new KnucklesTheEchidna());
        addArtifacts(player1, 30);

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Does not trigger at upkeep with only twenty-nine artifacts")
    void doesNotTriggerWithTwentyNineArtifacts() {
        harness.addToBattlefield(player1, new KnucklesTheEchidna());
        addArtifacts(player1, 29);

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Opponent's artifacts do not count toward the win condition")
    void opponentArtifactsDoNotCount() {
        harness.addToBattlefield(player1, new KnucklesTheEchidna());
        addArtifacts(player1, 15);
        addArtifacts(player2, 15);

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    private void resolveCombatUnblocked() {
        gs.declareBlockers(gd, player2, List.of());
        harness.passUntil(player1, TurnStep.END_COMBAT);
    }

    private long treasureCount(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(perm -> perm.getCard().getSubtypes().contains(CardSubtype.TREASURE))
                .count();
    }

    private void addArtifacts(Player player, int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player, new LeoninScimitar());
        }
    }

    @Test
    void simultaneousCreatureDamageCreatesOnlyOneTreasure() {
        harness.addToBattlefield(player1, new KnucklesTheEchidna());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(1, 2));
        resolveCombatUnblocked();

        assertThat(treasureCount(player1)).isEqualTo(1);
        assertThat(treasureCount(player2)).isZero();
    }

    @Test
    void opponentCreatureDamageDoesNotCreateTreasure() {
        harness.addToBattlefield(player1, new KnucklesTheEchidna());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of());
        harness.passUntil(player2, TurnStep.END_COMBAT);

        assertThat(treasureCount(player1)).isZero();
    }

    @Test
    void doesNotWinIfArtifactCountDropsBeforeResolution() {
        harness.addToBattlefield(player1, new KnucklesTheEchidna());
        addArtifacts(player1, 30);
        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);

        Permanent artifact = gd.playerBattlefields.get(player1.getId()).removeLast();
        gd.playerGraveyards.get(player1.getId()).add(artifact.getCard());
        harness.passBothPriorities();

        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotTriggerDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new KnucklesTheEchidna());
        addArtifacts(player1, 30);

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    void upkeepAbilityStillWinsAfterKnucklesLeavesBattlefield() {
        Permanent knuckles = harness.addToBattlefieldAndReturn(player1, new KnucklesTheEchidna());
        addArtifacts(player1, 30);
        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);

        gd.playerBattlefields.get(player1.getId()).remove(knuckles);
        gd.playerGraveyards.get(player1.getId()).add(knuckles.getCard());
        harness.passBothPriorities();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player1.getId());
    }
}
