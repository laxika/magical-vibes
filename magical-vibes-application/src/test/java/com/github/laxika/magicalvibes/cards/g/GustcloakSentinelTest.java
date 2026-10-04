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

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();
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

        declareAttackersAndPrepareBlockers(List.of(0));
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

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        resolveAllTriggers();

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

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(sentinel.isTapped()).isTrue();
        assertThat(sentinel.isAttacking()).isTrue();
        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("An already untapped Sentinel can still be removed from combat")
    void alreadyUntappedSentinelCanBeRemovedFromCombat() {
        Permanent sentinel = addSentinel();
        addCreatureReady(player2, new GlorySeeker());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        sentinel.untap();
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(sentinel.isTapped()).isFalse();
        assertThat(sentinel.isAttacking()).isFalse();
        assertThat(sentinel.getAttackTarget()).isNull();
    }

    @Test
    @DisplayName("Removing Sentinel from combat prevents damage in both directions")
    void acceptingTriggerPreventsCombatDamage() {
        Permanent sentinel = addSentinel();
        Permanent blocker = addCreatureReady(player2, new GlorySeeker());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(sentinel);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
        assertThat(sentinel.getMarkedDamage()).isZero();
        assertThat(blocker.getMarkedDamage()).isZero();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    private Permanent addSentinel() {
        return addCreatureReady(player1, new GustcloakSentinel());
    }
}
