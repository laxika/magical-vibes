package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.c.CoastalBulwark;
import com.github.laxika.magicalvibes.cards.c.CombatCourier;
import com.github.laxika.magicalvibes.cards.e.EnergyRefractor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BlastZone.class, Forest.class, CoastalBulwark.class, CombatCourier.class, EnergyRefractor.class})
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
        harness.addToBattlefield(player2, new CoastalBulwark());
        harness.addToBattlefield(player2, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Blast Zone");
        harness.assertInGraveyard(player2, "Coastal Bulwark");
        harness.assertOnBattlefield(player2, "Forest");
    }

    private Permanent addReadyBlastZone(Player player, int chargeCounters) {
        Permanent blastZone = harness.addToBattlefieldAndReturn(player, new BlastZone());
        blastZone.setCounterCount(CounterType.CHARGE, chargeCounters);
        return blastZone;
    }

    @Test
    void zeroXStillTapsWithoutAddingCounters() {
        Permanent blastZone = addReadyBlastZone(player1, 1);

        harness.activateAbility(player1, 0, 1, 0, null);
        assertThat(blastZone.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(blastZone.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    void cannotPayForXWithOnlyOneXWorthOfMana() {
        Permanent blastZone = addReadyBlastZone(player1, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, 2, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(blastZone.isTapped()).isFalse();
        assertThat(blastZone.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    void destroysMatchingCreaturesAndArtifactsOnBothBattlefieldsOnlyOnResolution() {
        addReadyBlastZone(player1, 2);
        harness.addToBattlefield(player1, new CoastalBulwark());
        harness.addToBattlefield(player2, new EnergyRefractor());
        harness.addToBattlefield(player2, new CombatCourier());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 2, null, null);

        harness.assertInGraveyard(player1, "Blast Zone");
        harness.assertOnBattlefield(player1, "Coastal Bulwark");
        harness.assertOnBattlefield(player2, "Energy Refractor");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Coastal Bulwark");
        harness.assertInGraveyard(player2, "Energy Refractor");
        harness.assertOnBattlefield(player2, "Combat Courier");
    }

    @Test
    void zeroCountersDoesNotDestroyLandsOrOneManaPermanents() {
        addReadyBlastZone(player1, 0);
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new CombatCourier());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Blast Zone");
        harness.assertOnBattlefield(player2, "Forest");
        harness.assertOnBattlefield(player2, "Combat Courier");
    }
}
