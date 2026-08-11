package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CanopySurgeTest extends BaseCardTest {

    @Test
    void unkickedDealsOneDamageToPlayersAndFlyingCreatures() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent ownFlyer = new Permanent(new AirElemental());
        Permanent opposingFlyer = new Permanent(new AirElemental());
        Permanent ownBear = new Permanent(new GrizzlyBears());
        Permanent opposingBear = new Permanent(new GrizzlyBears());
        harness.getGameData().playerBattlefields.get(player1.getId()).add(ownFlyer);
        harness.getGameData().playerBattlefields.get(player1.getId()).add(ownBear);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(opposingFlyer);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(opposingBear);
        harness.setHand(player1, List.of(new CanopySurge()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        assertThat(ownFlyer.getMarkedDamage()).isEqualTo(1);
        assertThat(opposingFlyer.getMarkedDamage()).isEqualTo(1);
        assertThat(ownBear.getMarkedDamage()).isZero();
        assertThat(opposingBear.getMarkedDamage()).isZero();
    }

    @Test
    void kickedDealsFourDamageToPlayersAndFlyingCreatures() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new AirElemental());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new AirElemental());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CanopySurge()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castKickedSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(16);
        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
        harness.assertNotOnBattlefield(player1, "Air Elemental");
        harness.assertNotOnBattlefield(player2, "Air Elemental");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }
}
