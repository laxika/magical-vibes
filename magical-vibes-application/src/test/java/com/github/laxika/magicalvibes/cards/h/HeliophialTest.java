package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class HeliophialTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing Heliophial deals damage equal to its charge counters to a player")
    void sacrificeDealsDamageToPlayer() {
        Permanent heliophial = addReadyHeliophial(player1);
        heliophial.setCounterCount(CounterType.CHARGE, 5);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
        harness.assertInGraveyard(player1, "Heliophial");
    }

    @Test
    @DisplayName("Sacrificing Heliophial deals damage to a creature")
    void sacrificeDealsDamageToCreature() {
        Permanent heliophial = addReadyHeliophial(player1);
        heliophial.setCounterCount(CounterType.CHARGE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.activateAbility(player1, 0, null, bearsId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Heliophial");
    }

    @Test
    @DisplayName("Sacrificing Heliophial with no charge counters deals no damage")
    void sacrificeWithNoCountersDealsNoDamage() {
        addReadyHeliophial(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        harness.assertInGraveyard(player1, "Heliophial");
    }

    @Test
    @DisplayName("Heliophial can activate while tapped because its ability has no tap cost")
    void abilityDoesNotRequireTapping() {
        Permanent heliophial = addReadyHeliophial(player1);
        heliophial.tap();
        heliophial.setCounterCount(CounterType.CHARGE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    private Permanent addReadyHeliophial(Player player) {
        Heliophial card = new Heliophial();
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
