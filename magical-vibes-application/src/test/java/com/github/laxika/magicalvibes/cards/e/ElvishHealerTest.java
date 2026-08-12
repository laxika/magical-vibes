package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.cards.n.NissaWorldwaker;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ElvishHealerTest extends BaseCardTest {

    private void addHealerReady() {
        harness.addToBattlefield(player1, new ElvishHealer());
        Permanent healer = findPermanent(player1, "Elvish Healer");
        healer.setSummoningSick(false);
    }

    @Test
    @DisplayName("Prevents 1 damage to a non-green creature")
    void preventsOneOnNonGreenCreature() {
        addHealerReady();
        harness.addToBattlefield(player2, new Memnite());

        UUID targetId = harness.getPermanentId(player2, "Memnite");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        Permanent memnite = findPermanent(player2, "Memnite");
        assertThat(memnite.getDamagePreventionShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Prevents 2 damage to a green creature")
    void preventsTwoOnGreenCreature() {
        addHealerReady();
        harness.addToBattlefield(player2, new GrizzlyBears());

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        Permanent bears = findPermanent(player2, "Grizzly Bears");
        assertThat(bears.getDamagePreventionShield()).isEqualTo(2);
    }

    @Test
    @DisplayName("Prevents 1 damage to a green planeswalker")
    void preventsOneOnGreenPlaneswalker() {
        addHealerReady();
        Permanent nissa = harness.addToBattlefieldAndReturn(player2, new NissaWorldwaker());
        nissa.setCounterCount(CounterType.LOYALTY, 5);

        harness.activateAbility(player1, 0, null, nissa.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Nissa, Worldwaker").getDamagePreventionShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Prevents 1 damage to a player")
    void preventsOneOnPlayer() {
        addHealerReady();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isEqualTo(1);
    }
}
