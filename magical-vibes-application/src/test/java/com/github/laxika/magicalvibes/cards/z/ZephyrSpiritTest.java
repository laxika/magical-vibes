package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.g.GlassGolem;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ZephyrSpirit.class, GlassGolem.class})
class ZephyrSpiritTest extends BaseCardTest {

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
