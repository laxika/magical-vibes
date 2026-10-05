package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.c.CloudcrownOak;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OakgnarlWarrior.class, CloudcrownOak.class})
class OakgnarlWarriorTest extends BaseCardTest {

    @Test
    void vigilanceLeavesAttackerUntapped() {
        Permanent warrior = addCreatureReady(player1, new OakgnarlWarrior());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));

        assertThat(warrior.isAttacking()).isTrue();
        assertThat(warrior.isTapped()).isFalse();
    }

    @Test
    void trampleDealsExcessDamageToDefendingPlayer() {
        Permanent blocker = prepareBlockedCombat();

        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 4, player2.getId(), 1));

        harness.assertLife(player2, 19);
        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player2, "Cloudcrown Oak");
        assertThat(findPermanent(player1, "Oakgnarl Warrior").isTapped()).isFalse();
    }

    @Test
    void trampleRequiresLethalDamageBeforeAssigningDamageToPlayer() {
        Permanent blocker = prepareBlockedCombat();

        assertThatThrownBy(() -> harness.handleCombatDamageAssigned(player1, 0,
                Map.of(blocker.getId(), 3, player2.getId(), 2)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Trample: must assign at least 4 damage");

        harness.assertLife(player2, 20);
        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 4, player2.getId(), 1));
        harness.assertLife(player2, 19);
    }

    @Test
    void mayAssignAllDamageToBlocker() {
        Permanent blocker = prepareBlockedCombat();

        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 5));

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player2, "Cloudcrown Oak");
        assertThat(findPermanent(player1, "Oakgnarl Warrior").isTapped()).isFalse();
    }

    private Permanent prepareBlockedCombat() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new OakgnarlWarrior());
        Permanent blocker = addCreatureReady(player2, new CloudcrownOak());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.CombatDamageAssignment.class);
        return blocker;
    }
}
