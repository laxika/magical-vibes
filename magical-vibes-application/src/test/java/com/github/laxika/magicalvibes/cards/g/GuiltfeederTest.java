package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BalthorTheDefiled;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BalthorTheDefiled.class, GiantWarthog.class, Guiltfeeder.class})
class GuiltfeederTest extends BaseCardTest {

    private Permanent addAttacker() {
        Permanent attacker = addCreatureReady(player1, new Guiltfeeder());
        attacker.setAttacking(true);
        return attacker;
    }

    private void declareBlockers(List<BlockerAssignment> assignments) {
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, assignments);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Unblocked Guiltfeeder makes the defending player lose life equal to their graveyard size")
    void unblockedLifeLossEqualsDefendingGraveyardSize() {
        harness.setGraveyard(player1, List.of(new BalthorTheDefiled(), new GiantWarthog()));
        harness.setGraveyard(player2, List.of(
                new BalthorTheDefiled(), new GiantWarthog(), new BalthorTheDefiled()));
        addAttacker();

        int startingLife = gd.getLife(player2.getId());
        declareBlockers(List.of());

        assertThat(gd.getLife(player2.getId())).isEqualTo(startingLife - 3);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Blocked Guiltfeeder does not trigger")
    void blockedDoesNotTrigger() {
        harness.setGraveyard(player2, List.of(new BalthorTheDefiled(), new GiantWarthog()));
        Permanent attacker = addAttacker();
        Permanent blocker = addCreatureReady(player2, new BalthorTheDefiled());

        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int startingLife = gd.getLife(player2.getId());

        declareBlockers(List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(gd.getLife(player2.getId())).isEqualTo(startingLife);
    }

    @Test
    @DisplayName("Fear prevents a nonblack, nonartifact creature from blocking Guiltfeeder")
    void fearPreventsNonblackNonartifactBlocker() {
        Permanent attacker = addAttacker();
        Permanent blocker = addCreatureReady(player2, new GiantWarthog());

        prepareDeclareBlockers();

        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("fear");
    }

    @Test
    @DisplayName("An empty defending graveyard causes no life loss")
    void emptyDefendingGraveyardCausesNoLifeLoss() {
        harness.setGraveyard(player1, List.of(new Guiltfeeder()));
        harness.setGraveyard(player2, List.of());
        addAttackerForJudReview();

        int startingLife = gd.getLife(player2.getId());
        int controllerStartingLife = gd.getLife(player1.getId());
        declareBlockers(List.of());

        assertThat(gd.getLife(player2.getId())).isEqualTo(startingLife);
        assertThat(gd.getLife(player1.getId())).isEqualTo(controllerStartingLife);
    }

    @Test
    @DisplayName("The trigger counts the defending graveyard when it resolves")
    void countsDefendingGraveyardAtResolution() {
        harness.setGraveyard(player2, List.of(new Guiltfeeder()));
        addAttackerForJudReview();

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.setGraveyard(player2, List.of(
                new Guiltfeeder(), new Guiltfeeder(), new Guiltfeeder(), new Guiltfeeder()));

        int startingLife = gd.getLife(player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(startingLife - 4);
    }

    private Permanent addAttackerForJudReview() {
        Permanent attacker = addCreatureReady(player1, new Guiltfeeder());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        return attacker;
    }
}
