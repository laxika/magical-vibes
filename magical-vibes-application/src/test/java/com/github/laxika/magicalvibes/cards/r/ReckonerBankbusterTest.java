package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ReckonerBankbuster.class, GrizzlyBears.class})
class ReckonerBankbusterTest extends BaseCardTest {

    @Test
    void entersWithThreeChargeCounters() {
        harness.setHand(player1, List.of(new ReckonerBankbuster()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        Permanent bankbuster = findPermanent(player1, "Reckoner Bankbuster");
        assertThat(bankbuster.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
    }

    @Test
    void drawsAndRemovesChargeCounter() {
        Permanent bankbuster = addBankbusterWithCounters(3);
        GrizzlyBears drawnCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(bankbuster.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawnCard);
    }

    @Test
    void lastChargeCounterCreatesTreasureAndPilot() {
        Permanent bankbuster = addBankbusterWithCounters(1);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(bankbuster.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(findPermanents(player1, "Pilot")).hasSize(1);
    }

    @Test
    void pilotCanCrewVehicleWithItsPowerBonus() {
        Permanent bankbuster = addBankbusterWithCounters(1);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        Permanent pilot = findPermanent(player1, "Pilot");
        bankbuster.untap();
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(bankbuster), 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, bankbuster)).isTrue();
        assertThat(pilot.isTapped()).isTrue();
        assertThat(pilot.getCard().getSubtypes()).contains(CardSubtype.PILOT);
    }

    @Test
    void cannotActivateDrawAbilityWithoutChargeCounters() {
        Permanent bankbuster = addBankbusterWithCounters(0);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(bankbuster),
                0,
                null,
                null
        )).isInstanceOf(IllegalStateException.class);
    }

    private Permanent addBankbusterWithCounters(int counters) {
        Permanent bankbuster = harness.addToBattlefieldAndReturn(player1, new ReckonerBankbuster());
        bankbuster.setCounterCount(CounterType.CHARGE, counters);
        return bankbuster;
    }
}
