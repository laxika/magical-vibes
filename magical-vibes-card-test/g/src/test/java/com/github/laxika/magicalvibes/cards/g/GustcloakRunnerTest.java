package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.i.Inspirit;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GustcloakRunner.class, ElvishWarrior.class, Inspirit.class})
class GustcloakRunnerTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the becomes-blocked trigger untaps and removes the Runner from combat")
    void acceptingBecomesBlockedTriggerUntapsAndRemovesFromCombat() {
        Permanent runner = addRunner();
        Permanent blocker = addCreatureReady(player2);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();
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

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();
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

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));
        resolveAllTriggers();
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

    @Test
    @DisplayName("An already untapped Runner can still be removed from combat")
    void acceptingTriggerWhenAlreadyUntappedRemovesFromCombat() {
        Permanent runner = addRunner();
        Permanent blocker = addCreatureReady(player2);
        int startingLife = gd.getLife(player2.getId());
        harness.setHand(player1, List.of(new Inspirit()));

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.ensurePriority(player1);
        harness.castInstant(player1, 0, runner.getId());
        harness.passBothPriorities();
        assertThat(runner.isTapped()).isFalse();
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(runner.isTapped()).isFalse();
        assertThat(runner.isAttacking()).isFalse();
        assertThat(runner.getAttackTarget()).isNull();

        resolveCombat();

        assertThat(gd.getLife(player2.getId())).isEqualTo(startingLife);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(runner);
        assertThat(blocker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("An unblocked Runner deals combat damage without offering its ability")
    void unblockedRunnerDoesNotTrigger() {
        addRunner();
        addCreatureReady(player2);
        int startingLife = gd.getLife(player2.getId());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.getLife(player2.getId())).isEqualTo(startingLife - 1);
    }

    @Test
    @DisplayName("A blocking Runner does not trigger its becomes-blocked ability")
    void blockingRunnerDoesNotTrigger() {
        Permanent attacker = addCreatureReady(player1);
        Permanent runner = addCreatureReady(player2, new GustcloakRunner());
        int startingLife = gd.getLife(player2.getId());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.getLife(player2.getId())).isEqualTo(startingLife);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(runner);
        harness.assertInGraveyard(player2, "Gustcloak Runner");
        assertThat(attacker.getMarkedDamage()).isEqualTo(1);
    }

    private Permanent addRunner() {
        return addCreatureReady(player1, new GustcloakRunner());
    }

    private Permanent addCreatureReady(com.github.laxika.magicalvibes.model.Player player) {
        return addCreatureReady(player, new ElvishWarrior());
    }
}
