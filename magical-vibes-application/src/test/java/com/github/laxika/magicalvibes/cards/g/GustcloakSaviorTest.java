package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.FleetingAven;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GustcloakSavior.class, GlorySeeker.class, FleetingAven.class})
class GustcloakSaviorTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the becomes-blocked trigger untaps and removes the Savior from combat")
    void acceptingBecomesBlockedTriggerUntapsAndRemovesFromCombat() {
        Permanent savior = addSavior();
        Permanent blocker = addCreatureReady(player2);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(savior.isTapped()).isFalse();
        assertThat(savior.isAttacking()).isFalse();
        assertThat(savior.getAttackTarget()).isNull();
        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Declining the becomes-blocked trigger leaves the Savior in combat")
    void decliningBecomesBlockedTriggerLeavesItInCombat() {
        Permanent savior = addSavior();
        Permanent blocker = addCreatureReady(player2);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(savior.isTapped()).isTrue();
        assertThat(savior.isAttacking()).isTrue();
        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Accepting the trigger untaps and removes another creature you control from combat")
    void acceptingTriggerUntapsAndRemovesAnotherControlledCreatureFromCombat() {
        Permanent savior = addSavior();
        Permanent attacker = addCreatureReady(player1, new GlorySeeker());
        Permanent firstBlocker = addCreatureReady(player2);
        Permanent secondBlocker = addCreatureReady(player2);

        declareAttackersAndPrepareBlockers(List.of(1));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 1),
                new BlockerAssignment(1, 1)));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(attacker.isTapped()).isFalse();
        assertThat(attacker.isAttacking()).isFalse();
        assertThat(attacker.getAttackTarget()).isNull();
        assertThat(savior.isTapped()).isFalse();
        assertThat(firstBlocker.isBlocking()).isTrue();
        assertThat(secondBlocker.isBlocking()).isTrue();
    }

    private Permanent addSavior() {
        return addCreatureReady(player1, new GustcloakSavior());
    }

    private Permanent addCreatureReady(com.github.laxika.magicalvibes.model.Player player) {
        return addCreatureReady(player, new FleetingAven());
    }
}
