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
        resolveAllTriggers();
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
        resolveAllTriggers();
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
        resolveAllTriggers();

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

    @Test
    @DisplayName("A queued trigger still rescues another creature after the Savior leaves")
    void triggerResolvesAfterSaviorLeavesBattlefield() {
        Permanent savior = addSavior();
        Permanent attacker = addCreatureReady(player1, new GlorySeeker());
        addCreatureReady(player2);

        declareAttackersAndPrepareBlockers(List.of(1));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        gd.playerBattlefields.get(player1.getId()).remove(savior);
        gd.playerGraveyards.get(player1.getId()).add(savior.getCard());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(attacker.isTapped()).isFalse();
        assertThat(attacker.isAttacking()).isFalse();
        assertThat(attacker.getAttackTarget()).isNull();
    }

    @Test
    @DisplayName("The Savior does not trigger when an opponent's creature becomes blocked")
    void doesNotTriggerForOpponentsBlockedCreature() {
        addSavior();
        addCreatureReady(player2);

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        resolveCombat(player2);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player2, "Fleeting Aven");
        harness.assertOnBattlefield(player1, "Gustcloak Savior");
    }

    @Test
    @DisplayName("An unblocked attacker does not trigger the Savior")
    void doesNotTriggerForUnblockedCreature() {
        addSavior();
        addCreatureReady(player1, new GlorySeeker());
        addCreatureReady(player2);

        declareAttackersAndPrepareBlockers(List.of(1));
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 18);
    }

    private Permanent addSavior() {
        return addCreatureReady(player1, new GustcloakSavior());
    }

    private Permanent addCreatureReady(com.github.laxika.magicalvibes.model.Player player) {
        return addCreatureReady(player, new FleetingAven());
    }
}
