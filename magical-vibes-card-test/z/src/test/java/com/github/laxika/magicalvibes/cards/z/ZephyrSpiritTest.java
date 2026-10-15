package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.g.GlassGolem;
import com.github.laxika.magicalvibes.cards.h.HighGround;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ZephyrSpirit.class, GlassGolem.class, HighGround.class})
class ZephyrSpiritTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking does not trigger Zephyr Spirit's return ability")
    void attackingDoesNotReturnToHand() {
        addCreatureReady(player1, new ZephyrSpirit());
        addCreatureReady(player2, new GlassGolem());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Zephyr Spirit");
        harness.assertNotInHand(player1, "Zephyr Spirit");
    }

    @Test
    @DisplayName("The blocked attacker deals no damage after Zephyr Spirit returns to hand")
    void blockedAttackerDealsNoDamageAfterSpiritReturns() {
        addCreatureReady(player1, new GlassGolem());
        addCreatureReady(player2, new ZephyrSpirit());
        harness.setLife(player2, 20);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).hasSize(1);
        harness.assertOnBattlefield(player2, "Zephyr Spirit");
        harness.assertNotInHand(player2, "Zephyr Spirit");

        resolveAllTriggers();
        harness.assertInHand(player2, "Zephyr Spirit");
        resolveCombat();

        harness.assertLife(player2, 20);
        harness.assertNotInGraveyard(player2, "Zephyr Spirit");
    }

    @Test
    @DisplayName("Blocking multiple creatures triggers the return ability only once")
    @CardUsed(HighGround.class)
    void blockingMultipleCreaturesTriggersOnlyOnce() {
        addCreatureReady(player1, new GlassGolem());
        addCreatureReady(player1, new GlassGolem());
        addCreatureReady(player2, new ZephyrSpirit());
        harness.addToBattlefield(player2, new HighGround());

        declareAttackersAndPrepareBlockers(List.of(0, 1));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(0, 1)));

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        harness.assertInHand(player2, "Zephyr Spirit");
        harness.assertNotOnBattlefield(player2, "Zephyr Spirit");
    }

    @Test
    @DisplayName("When Zephyr Spirit blocks, it returns to its owner's hand")
    void returnsToOwnersHandWhenBlocking() {
        Permanent attacker = addCreatureReady(player1, new GlassGolem());
        attacker.setAttacking(true);
        addCreatureReady(player2, new ZephyrSpirit());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.assertInHand(player2, "Zephyr Spirit");
        harness.assertNotOnBattlefield(player2, "Zephyr Spirit");
    }

    @Test
    @DisplayName("Zephyr Spirit stays on the battlefield when it does not block")
    void staysOnBattlefieldWhenNotBlocking() {
        Permanent attacker = addCreatureReady(player1, new GlassGolem());
        attacker.setAttacking(true);
        addCreatureReady(player2, new ZephyrSpirit());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Zephyr Spirit");
    }

    @Test
    @DisplayName("When controlled by another player, Zephyr Spirit returns to its owner's hand")
    void returnsToOwnersHandWhenControlledByAnotherPlayer() {
        Permanent attacker = addCreatureReady(player1, new GlassGolem());
        attacker.setAttacking(true);

        ZephyrSpirit spiritCard = new ZephyrSpirit();
        spiritCard.setOwnerId(player1.getId());
        addCreatureReady(player2, spiritCard);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Zephyr Spirit");
        harness.assertNotInHand(player2, "Zephyr Spirit");
        harness.assertNotOnBattlefield(player2, "Zephyr Spirit");
    }
}
