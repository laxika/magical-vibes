package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShopkeepersBane.class})
class ShopkeepersBaneTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking with Shopkeeper's Bane gains its controller 2 life")
    void attackGainsTwoLife() {
        harness.setLife(player1, 20);
        addCreatureReady(player1, new ShopkeepersBane());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Each attacking copy gains life, while a nonattacking copy does not")
    void eachAttackingCopyGainsLife() {
        harness.setLife(player1, 20);
        addCreatureReady(player1, new ShopkeepersBane());
        addCreatureReady(player1, new ShopkeepersBane());
        addCreatureReady(player1, new ShopkeepersBane());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(24);
    }

    @Test
    @DisplayName("The attack trigger gains life for the attacking controller on either side")
    void opponentControllerGainsLife() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new ShopkeepersBane());
        addCreatureReady(player2, new ShopkeepersBane());

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(22);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Trample deals excess damage through a blocker without gaining life from damage or blocking")
    void trampleDealsExcessDamageWithoutAdditionalLifeGain() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent attacker = addCreatureReady(player1, new ShopkeepersBane());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new ShopkeepersBane());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2,
                player2.getId(), 2
        ));
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        harness.assertInGraveyard(player1, "Shopkeeper's Bane");
        harness.assertInGraveyard(player2, "Shopkeeper's Bane");
    }
}
