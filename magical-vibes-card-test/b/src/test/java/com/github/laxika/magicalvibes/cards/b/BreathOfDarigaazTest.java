package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.ObsianusGolem;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BreathOfDarigaaz.class, AirElemental.class, GrizzlyBears.class, ObsianusGolem.class})
class BreathOfDarigaazTest extends BaseCardTest {

    @Test
    void unkickedDealsOneDamageToPlayersAndNonFlyingCreatures() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new AirElemental());
        harness.castFromHand(player1, new BreathOfDarigaaz(), "{1}{R}");
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 19);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Air Elemental");
    }

    @Test
    void kickedDealsFourDamageToPlayersAndNonFlyingCreatures() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new AirElemental());
        harness.setHand(player1, List.of(new BreathOfDarigaaz()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castKickedSorcery(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 16);
        harness.assertLife(player2, 16);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Air Elemental");
    }

    @Test
    void unkickedMarksDamageOnNonFlyingCreaturesOnly() {
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent opposingAirElemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.castFromHand(player1, new BreathOfDarigaaz(), "{1}{R}");
        harness.passBothPriorities();

        assertThat(ownBear.getMarkedDamage()).isEqualTo(1);
        assertThat(opposingBear.getMarkedDamage()).isEqualTo(1);
        assertThat(opposingAirElemental.getMarkedDamage()).isZero();
    }

    @Test
    void unkickedDamagesBothPlayersWithNoCreatures() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.castFromHand(player1, new BreathOfDarigaaz(), "{1}{R}");
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 19);
        harness.assertInGraveyard(player1, "Breath of Darigaaz");
    }

    @Test
    void kickedDamagesBothPlayersWithNoCreatures() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new BreathOfDarigaaz()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castKickedSorcery(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 16);
        harness.assertLife(player2, 16);
        harness.assertInGraveyard(player1, "Breath of Darigaaz");
    }

    @Test
    void kickedMarksExactlyFourDamageOnSurvivingCreaturesAndNoneOnFlyers() {
        Permanent ownGolem = harness.addToBattlefieldAndReturn(player1, new ObsianusGolem());
        Permanent opposingGolem = harness.addToBattlefieldAndReturn(player2, new ObsianusGolem());
        Permanent ownFlyer = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        Permanent opposingFlyer = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new BreathOfDarigaaz()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castKickedSorcery(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Obsianus Golem");
        harness.assertOnBattlefield(player2, "Obsianus Golem");
        harness.assertOnBattlefield(player1, "Air Elemental");
        harness.assertOnBattlefield(player2, "Air Elemental");
        assertThat(ownGolem.getMarkedDamage()).isEqualTo(4);
        assertThat(opposingGolem.getMarkedDamage()).isEqualTo(4);
        assertThat(ownFlyer.getMarkedDamage()).isZero();
        assertThat(opposingFlyer.getMarkedDamage()).isZero();
    }

}
