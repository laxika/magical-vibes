package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RakdosFirewheeler.class, GrizzlyBears.class, ChandraNalaar.class})
class RakdosFirewheelerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB deals 2 damage to the opponent and 2 damage to a target creature")
    void etbDamagesOpponentAndCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castWithTargets(harness.getPermanentId(player2, "Grizzly Bears"));

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("ETB deals 2 damage to a target planeswalker")
    void etbDamagesPlaneswalker() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);
        castWithTargets(planeswalker.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("ETB deals damage to the opponent without choosing the optional permanent target")
    void etbDamagesOpponentWithoutPermanentTarget() {
        castWithoutPermanentTarget();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Optional permanent target becoming illegal does not stop the opponent damage")
    void illegalOptionalTargetDoesNotStopOpponentDamage() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID creatureId = harness.getPermanentId(player2, "Grizzly Bears");
        castWithTargets(creatureId);

        harness.passBothPriorities();
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("ETB can damage a creature controlled by its controller")
    void etbCanDamageOwnCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new RakdosFirewheeler());
        castWithTargets(creature.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isEqualTo(2);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("ETB can omit the permanent target even when a creature is available")
    void etbCanOmitAvailablePermanentTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new RakdosFirewheeler());
        castWithoutPermanentTarget();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isZero();
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("ETB still deals both amounts of damage after its source leaves")
    void etbResolvesAfterSourceLeaves() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new RakdosFirewheeler());
        castWithTargets(creature.getId());

        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isEqualTo(2);
        harness.assertLife(player2, 18);
    }

    private void castWithTargets(UUID permanentTargetId) {
        harness.setHand(player1, List.of(new RakdosFirewheeler()));
        addRakdosMana();
        harness.castCreature(player1, 0, List.of(player2.getId(), permanentTargetId));
    }

    private void castWithoutPermanentTarget() {
        harness.setHand(player1, List.of(new RakdosFirewheeler()));
        addRakdosMana();
        harness.castCreature(player1, 0, List.of(player2.getId()));
    }

    private void addRakdosMana() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.RED, 2);
    }
}
