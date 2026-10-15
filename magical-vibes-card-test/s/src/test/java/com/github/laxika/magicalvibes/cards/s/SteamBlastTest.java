package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CoralMerfolk;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.PendrellDrake;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SteamBlast.class, CoralMerfolk.class, Forest.class, PendrellDrake.class})
class SteamBlastTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage to each creature and each player")
    void damagesEveryCreatureAndPlayer() {
        harness.addToBattlefield(player1, new CoralMerfolk());
        harness.addToBattlefield(player2, new CoralMerfolk());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castFromHand(player1, new SteamBlast(), "{2}{R}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Coral Merfolk");
        harness.assertNotOnBattlefield(player2, "Coral Merfolk");
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Does not damage noncreature permanents")
    void doesNotDamageNoncreaturePermanents() {
        harness.addToBattlefield(player2, new Forest());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castFromHand(player1, new SteamBlast(), "{2}{R}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Forest");
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Damages flying creatures on both sides and accumulates damage from successive blasts")
    void damagesFlyingCreaturesAndAccumulatesDamage() {
        harness.addToBattlefield(player1, new PendrellDrake());
        harness.addToBattlefield(player2, new PendrellDrake());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castFromHand(player1, new SteamBlast(), "{2}{R}");
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Pendrell Drake").getMarkedDamage()).isEqualTo(2);
        assertThat(findPermanent(player2, "Pendrell Drake").getMarkedDamage()).isEqualTo(2);
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);

        harness.castFromHand(player1, new SteamBlast(), "{2}{R}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Pendrell Drake");
        harness.assertNotOnBattlefield(player2, "Pendrell Drake");
        harness.assertInGraveyard(player1, "Pendrell Drake");
        harness.assertInGraveyard(player2, "Pendrell Drake");
        harness.assertLife(player1, 16);
        harness.assertLife(player2, 16);
    }
}
