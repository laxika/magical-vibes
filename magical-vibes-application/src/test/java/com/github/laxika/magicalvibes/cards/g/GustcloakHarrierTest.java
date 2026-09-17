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

@CardUsed({GustcloakHarrier.class, FleetingAven.class})
class GustcloakHarrierTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the becomes-blocked trigger untaps and removes the Harrier from combat")
    void acceptingBecomesBlockedTriggerUntapsAndRemovesFromCombat() {
        Permanent harrier = addHarrier();
        Permanent blocker = addCreatureReady(player2);

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(harrier.isTapped()).isFalse();
        assertThat(harrier.isAttacking()).isFalse();
        assertThat(harrier.getAttackTarget()).isNull();
        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Declining the becomes-blocked trigger leaves the Harrier in combat")
    void decliningBecomesBlockedTriggerLeavesItInCombat() {
        Permanent harrier = addHarrier();
        Permanent blocker = addCreatureReady(player2);

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(harrier.isTapped()).isTrue();
        assertThat(harrier.isAttacking()).isTrue();
        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Accepting the becomes-blocked trigger once untaps and removes the Harrier when multiple creatures block it")
    void acceptingBecomesBlockedTriggerOnceWithMultipleBlockers() {
        Permanent harrier = addHarrier();
        Permanent firstBlocker = addCreatureReady(player2);
        Permanent secondBlocker = addCreatureReady(player2);
        int startingLife = gd.getLife(player2.getId());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(harrier.isTapped()).isFalse();
        assertThat(harrier.isAttacking()).isFalse();
        assertThat(firstBlocker.isBlocking()).isTrue();
        assertThat(secondBlocker.isBlocking()).isTrue();

        resolveCombat();

        assertThat(gd.getLife(player2.getId())).isEqualTo(startingLife);
        assertThat(firstBlocker.getMarkedDamage()).isZero();
        assertThat(secondBlocker.getMarkedDamage()).isZero();
    }

    private Permanent addHarrier() {
        return addCreatureReady(player1, new GustcloakHarrier());
    }

    private Permanent addCreatureReady(com.github.laxika.magicalvibes.model.Player player) {
        return addCreatureReady(player, new FleetingAven());
    }
}
