package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.w.WallOfMist;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoblinLocksmith.class, WallOfMist.class, LlanowarElves.class, GenerousStray.class})
class GoblinLocksmithTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking makes creatures with defender unable to block this turn")
    void creaturesWithDefenderCannotBlock() {
        Permanent locksmith = addCreatureReady(player1, new GoblinLocksmith());
        Permanent wall = addCreatureReady(player2, new WallOfMist());

        declareAttackers(player1, List.of(0));
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, this::resolveAllTriggers);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
        assertThat(bls.canBlockAttacker(gd, wall, locksmith,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
    }

    @Test
    @DisplayName("Attacking does not stop creatures without defender from blocking")
    void creaturesWithoutDefenderCanBlock() {
        addCreatureReady(player1, new GoblinLocksmith());
        Permanent elves = addCreatureReady(player2, new LlanowarElves());

        declareAttackers(player1, List.of(0));
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, this::resolveAllTriggers);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(elves.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("A defender entering after the attack trigger resolves cannot block")
    void defenderEnteringAfterResolutionCannotBlock() {
        addCreatureReady(player1, new GoblinLocksmith());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player1, List.of(0));
            resolveAllTriggers();
        });
        addCreatureReady(player2, new WallOfMist());
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Defenders cannot block another attacker either")
    void defendersCannotBlockOtherAttackers() {
        addCreatureReady(player1, new GoblinLocksmith());
        addCreatureReady(player1, new GenerousStray());
        addCreatureReady(player2, new WallOfMist());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player1, List.of(0, 1));
            resolveAllTriggers();
        });
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A Locksmith that does not attack does not prevent defenders from blocking")
    void nonattackingLocksmithDoesNotRestrictBlocking() {
        addCreatureReady(player1, new GoblinLocksmith());
        addCreatureReady(player1, new GenerousStray());
        Permanent wall = addCreatureReady(player2, new WallOfMist());

        declareAttackersAndPrepareBlockers(player1, List.of(1));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(wall.isBlocking()).isTrue();
    }
}
