package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BlastZoneTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield with a charge counter")
    void entersWithChargeCounter() {
        harness.setHand(player1, List.of(new BlastZone()));

        harness.playLand(player1, 0);

        Permanent blastZone = findPermanent(player1, "Blast Zone");
        assertThat(blastZone.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Taps to add one colorless mana")
    void tapsForColorlessMana() {
        addReadyBlastZone(player1, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Pays twice X to put X charge counters on Blast Zone")
    void paysTwiceXToAddXChargeCounters() {
        Permanent blastZone = addReadyBlastZone(player1, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 1, 2, null);
        harness.passBothPriorities();

        assertThat(blastZone.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Sacrifices to destroy nonland permanents with matching mana value")
    void sacrificesToDestroyMatchingNonlandPermanents() {
        addReadyBlastZone(player1, 2);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Blast Zone");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Forest");
    }

    private Permanent addReadyBlastZone(Player player, int chargeCounters) {
        Permanent blastZone = new Permanent(new BlastZone());
        blastZone.setSummoningSick(false);
        blastZone.setCounterCount(CounterType.CHARGE, chargeCounters);
        gd.playerBattlefields.get(player.getId()).add(blastZone);
        return blastZone;
    }
}
