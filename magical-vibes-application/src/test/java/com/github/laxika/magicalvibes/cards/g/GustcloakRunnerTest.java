package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GustcloakRunner.class, ElvishWarrior.class})
class GustcloakRunnerTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the becomes-blocked trigger untaps and removes the Runner from combat")
    void acceptingBecomesBlockedTriggerUntapsAndRemovesFromCombat() {
        Permanent runner = addRunner();
        Permanent blocker = addCreatureReady(player2);

        declareAttackers(List.of(0));
        runner.tap();
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(runner.isTapped()).isFalse();
        assertThat(runner.isAttacking()).isFalse();
        assertThat(runner.getAttackTarget()).isNull();
        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Declining the becomes-blocked trigger leaves the Runner in combat")
    void decliningBecomesBlockedTriggerLeavesItInCombat() {
        Permanent runner = addRunner();
        Permanent blocker = addCreatureReady(player2);

        declareAttackers(List.of(0));
        runner.tap();
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(runner.isTapped()).isTrue();
        assertThat(runner.isAttacking()).isTrue();
        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Accepting the becomes-blocked trigger once untaps and removes the Runner when multiple creatures block it")
    void acceptingBecomesBlockedTriggerOnceWithMultipleBlockers() {
        Permanent runner = addRunner();
        Permanent firstBlocker = addCreatureReady(player2);
        Permanent secondBlocker = addCreatureReady(player2);
        int startingLife = gd.getLife(player2.getId());

        declareAttackers(List.of(0));
        runner.tap();
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(runner.isTapped()).isFalse();
        assertThat(runner.isAttacking()).isFalse();
        assertThat(firstBlocker.isBlocking()).isTrue();
        assertThat(secondBlocker.isBlocking()).isTrue();

        resolveCombat();

        assertThat(gd.getLife(player2.getId())).isEqualTo(startingLife);
        assertThat(firstBlocker.getMarkedDamage()).isZero();
        assertThat(secondBlocker.getMarkedDamage()).isZero();
    }

    private Permanent addRunner() {
        return addCreatureReady(player1, new GustcloakRunner());
    }

    private Permanent addCreatureReady(com.github.laxika.magicalvibes.model.Player player) {
        return addCreatureReady(player, new ElvishWarrior());
    }
}
