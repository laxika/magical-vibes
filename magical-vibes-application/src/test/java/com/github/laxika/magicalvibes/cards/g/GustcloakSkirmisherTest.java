package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.FleetingAven;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GustcloakSkirmisher.class, FleetingAven.class})
class GustcloakSkirmisherTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the becomes-blocked trigger untaps and removes the Skirmisher from combat")
    void acceptingBecomesBlockedTriggerUntapsAndRemovesFromCombat() {
        Permanent skirmisher = addSkirmisher();
        Permanent blocker = addCreatureReady(player2);

        declareAttackers(List.of(0));
        skirmisher.tap();
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(skirmisher.isTapped()).isFalse();
        assertThat(skirmisher.isAttacking()).isFalse();
        assertThat(skirmisher.getAttackTarget()).isNull();
        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Declining the becomes-blocked trigger leaves the Skirmisher in combat")
    void decliningBecomesBlockedTriggerLeavesItInCombat() {
        Permanent skirmisher = addSkirmisher();
        Permanent blocker = addCreatureReady(player2);

        declareAttackers(List.of(0));
        skirmisher.tap();
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(skirmisher.isTapped()).isTrue();
        assertThat(skirmisher.isAttacking()).isTrue();
        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("An unblocked Skirmisher does not trigger its becomes-blocked ability")
    void unblockedSkirmisherDoesNotTrigger() {
        Permanent skirmisher = addSkirmisher();

        declareAttackers(List.of(0));
        skirmisher.tap();
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(skirmisher.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Accepting the becomes-blocked trigger once untaps and removes the Skirmisher when multiple creatures block it")
    void acceptingBecomesBlockedTriggerOnceWithMultipleBlockers() {
        Permanent skirmisher = addSkirmisher();
        Permanent firstBlocker = addCreatureReady(player2);
        Permanent secondBlocker = addCreatureReady(player2);
        int startingLife = gd.getLife(player2.getId());

        declareAttackers(List.of(0));
        skirmisher.tap();
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(skirmisher.isTapped()).isFalse();
        assertThat(skirmisher.isAttacking()).isFalse();
        assertThat(firstBlocker.isBlocking()).isTrue();
        assertThat(secondBlocker.isBlocking()).isTrue();

        resolveCombat();

        assertThat(gd.getLife(player2.getId())).isEqualTo(startingLife);
        assertThat(firstBlocker.getMarkedDamage()).isZero();
        assertThat(secondBlocker.getMarkedDamage()).isZero();
    }

    private Permanent addSkirmisher() {
        return addCreatureReady(player1, new GustcloakSkirmisher());
    }

    private Permanent addCreatureReady(com.github.laxika.magicalvibes.model.Player player) {
        return addCreatureReady(player, new FleetingAven());
    }
}
