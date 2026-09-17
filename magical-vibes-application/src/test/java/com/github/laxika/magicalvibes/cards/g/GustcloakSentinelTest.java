package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GustcloakSentinel.class, GlorySeeker.class})
class GustcloakSentinelTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the becomes-blocked trigger untaps and removes the Sentinel from combat")
    void acceptingBecomesBlockedTriggerUntapsAndRemovesFromCombat() {
        Permanent sentinel = addSentinel();
        Permanent blocker = addCreatureReady(player2, new GlorySeeker());

        declareAttackers(List.of(0));
        sentinel.tap();
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(sentinel.isTapped()).isFalse();
        assertThat(sentinel.isAttacking()).isFalse();
        assertThat(sentinel.getAttackTarget()).isNull();
        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("An unblocked Sentinel does not trigger its becomes-blocked ability")
    void unblockedSentinelDoesNotTrigger() {
        Permanent sentinel = addSentinel();
        addCreatureReady(player2, new GlorySeeker());

        declareAttackers(List.of(0));
        sentinel.tap();
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(sentinel.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Blocking by multiple creatures still creates only one may choice")
    void multipleBlockersCreateOnlyOneMayChoice() {
        Permanent sentinel = addSentinel();
        addCreatureReady(player2, new GlorySeeker());
        addCreatureReady(player2, new GlorySeeker());

        declareAttackers(List.of(0));
        sentinel.tap();
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(sentinel.isTapped()).isFalse();
        assertThat(sentinel.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("Declining the becomes-blocked trigger leaves the Sentinel in combat")
    void decliningBecomesBlockedTriggerLeavesItInCombat() {
        Permanent sentinel = addSentinel();
        Permanent blocker = addCreatureReady(player2, new GlorySeeker());

        declareAttackers(List.of(0));
        sentinel.tap();
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(sentinel.isTapped()).isTrue();
        assertThat(sentinel.isAttacking()).isTrue();
        assertThat(blocker.isBlocking()).isTrue();
    }

    private Permanent addSentinel() {
        return addCreatureReady(player1, new GustcloakSentinel());
    }
}
