package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

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

    private void castWithTargets(UUID permanentTargetId) {
        harness.setHand(player1, List.of(new RakdosFirewheeler()));
        addRakdosMana();
        gs.playCard(gd, player1, 0, 0, null, null,
                List.of(player2.getId(), permanentTargetId), List.of());
    }

    private void castWithoutPermanentTarget() {
        harness.setHand(player1, List.of(new RakdosFirewheeler()));
        addRakdosMana();
        gs.playCard(gd, player1, 0, 0, null, null,
                List.of(player2.getId()), List.of());
    }

    private void addRakdosMana() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.RED, 2);
    }
}
