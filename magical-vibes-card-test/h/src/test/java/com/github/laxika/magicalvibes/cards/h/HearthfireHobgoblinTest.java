package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.n.NipGwyllion;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HearthfireHobgoblin.class, HatchetBully.class, NipGwyllion.class})
class HearthfireHobgoblinTest extends BaseCardTest {

    @Test
    void unblockedHobgoblinDealsDamageInBothSteps() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new HearthfireHobgoblin());

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 16);
        harness.assertOnBattlefield(player1, "Hearthfire Hobgoblin");
    }

    @Test
    void blockerKilledInFirstDamageStepDealsNoDamageAndAttackerRemainsBlocked() {
        harness.setLife(player2, 20);
        Permanent attacker = addCreatureReady(player1, new HearthfireHobgoblin());
        addCreatureReady(player2, new NipGwyllion());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player2, "Nip Gwyllion");
        harness.assertNotOnBattlefield(player2, "Nip Gwyllion");
        harness.assertOnBattlefield(player1, "Hearthfire Hobgoblin");
        assertThat(attacker.getMarkedDamage()).isZero();
        harness.assertLife(player2, 20);
    }

    @Test
    void opposingHobgoblinsDieInFirstDamageStepWithoutDamagingPlayer() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new HearthfireHobgoblin());
        addCreatureReady(player2, new HearthfireHobgoblin());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Hearthfire Hobgoblin");
        harness.assertInGraveyard(player2, "Hearthfire Hobgoblin");
        harness.assertNotOnBattlefield(player1, "Hearthfire Hobgoblin");
        harness.assertNotOnBattlefield(player2, "Hearthfire Hobgoblin");
        harness.assertLife(player2, 20);
    }

    @Test
    void attackingHobgoblinDealsSecondDamageToSurvivingBlocker() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new HearthfireHobgoblin());
        addCreatureReady(player2, new HatchetBully());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Hearthfire Hobgoblin");
        harness.assertInGraveyard(player2, "Hatchet Bully");
        harness.assertNotOnBattlefield(player1, "Hearthfire Hobgoblin");
        harness.assertNotOnBattlefield(player2, "Hatchet Bully");
        harness.assertLife(player2, 20);
    }

    @Test
    void blockingHobgoblinDealsDamageInBothSteps() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new HatchetBully());
        addCreatureReady(player2, new HearthfireHobgoblin());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Hatchet Bully");
        harness.assertInGraveyard(player2, "Hearthfire Hobgoblin");
        harness.assertNotOnBattlefield(player1, "Hatchet Bully");
        harness.assertNotOnBattlefield(player2, "Hearthfire Hobgoblin");
        harness.assertLife(player2, 20);
    }
}
