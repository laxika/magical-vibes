package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AlmightyBrushwagg;
import com.github.laxika.magicalvibes.cards.h.HoneyMammoth;
import com.github.laxika.magicalvibes.cards.h.HumbleNaturalist;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ProudWildbonder.class, HumbleNaturalist.class, HoneyMammoth.class, AlmightyBrushwagg.class})
class ProudWildbonderTest extends BaseCardTest {

    @Test
    void unblockedTramplerDamagesPlayerWithoutOfferingDamageToOtherCreatures() {
        harness.setLife(player2, 20);
        Permanent bonder = addCreatureReady(player1, new ProudWildbonder());
        addCreatureReady(player2, new HumbleNaturalist());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.CombatDamageAssignment.class)).isNull();
        harness.assertLife(player2, 16);
        harness.assertOnBattlefield(player2, "Humble Naturalist");
        assertThat(bonder.getMarkedDamage()).isZero();
    }

    @Test
    void nonTrampleCreatureDoesNotGetAbility() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new ProudWildbonder());
        addCreatureReady(player1, new HumbleNaturalist());
        addCreatureReady(player2, new HumbleNaturalist());

        declareAttackersAndPrepareBlockers(List.of(1));
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.CombatDamageAssignment.class)).isNull();
        harness.assertLife(player2, 19);
        harness.assertOnBattlefield(player2, "Humble Naturalist");
    }

    @Test
    void opponentTrampleCreatureDoesNotGetAbility() {
        harness.setLife(player1, 20);
        addCreatureReady(player1, new ProudWildbonder());
        addCreatureReady(player1, new HumbleNaturalist());
        addCreatureReady(player2, new AlmightyBrushwagg());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.CombatDamageAssignment.class)).isNull();
        harness.assertLife(player1, 19);
    }

    @Test
    void blockedWildbonderCanAssignAllDamageToPlayerButStillReceivesBlockerDamage() {
        harness.setLife(player2, 20);
        Permanent bonder = addCreatureReady(player1, new ProudWildbonder());
        Permanent blocker = addCreatureReady(player2, new HumbleNaturalist());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(player2.getId(), 4));

        harness.assertLife(player2, 16);
        assertThat(blocker.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Humble Naturalist");
        assertThat(bonder.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void anotherTramplerCanBypassABlockerLargerThanItsPower() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new ProudWildbonder());
        addCreatureReady(player1, new AlmightyBrushwagg());
        Permanent blocker = addCreatureReady(player2, new HoneyMammoth());

        declareAttackersAndPrepareBlockers(List.of(1));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        harness.passBothPriorities();
        harness.handleCombatDamageAssigned(player1, 1, Map.of(player2.getId(), 1));

        harness.assertLife(player2, 19);
        assertThat(blocker.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Honey Mammoth");
        harness.assertInGraveyard(player1, "Almighty Brushwagg");
    }

    @Test
    void controllerMayDeclineBypassAndAssignDamageToBlocker() {
        harness.setLife(player2, 20);
        Permanent bonder = addCreatureReady(player1, new ProudWildbonder());
        Permanent blocker = addCreatureReady(player2, new HoneyMammoth());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 4));

        harness.assertLife(player2, 20);
        assertThat(blocker.getMarkedDamage()).isEqualTo(4);
        harness.assertOnBattlefield(player2, "Honey Mammoth");
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bonder);
        harness.assertInGraveyard(player1, "Proud Wildbonder");
    }
}
