package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.v.ViashinoRunner;
import com.github.laxika.magicalvibes.cards.z.Zephid;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FaultLine.class, ViashinoRunner.class, Zephid.class, Island.class})
class FaultLineTest extends BaseCardTest {

    @Test
    @DisplayName("Deals X damage to each player and each creature without flying")
    void damagesPlayersAndNonFlyingCreatures() {
        harness.addToBattlefield(player1, new ViashinoRunner());
        harness.addToBattlefield(player1, new Zephid());
        harness.addToBattlefield(player2, new ViashinoRunner());
        harness.addToBattlefield(player2, new Zephid());
        harness.addToBattlefield(player2, new Island());
        harness.setHand(player1, List.of(new FaultLine()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castInstant(player1, 0, 2, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Viashino Runner");
        harness.assertOnBattlefield(player1, "Zephid");
        harness.assertNotOnBattlefield(player2, "Viashino Runner");
        harness.assertOnBattlefield(player2, "Zephid");
        harness.assertOnBattlefield(player2, "Island");
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("X=0 deals no damage")
    void xZeroDealsNoDamage() {
        harness.addToBattlefield(player2, new ViashinoRunner());
        harness.setHand(player1, List.of(new FaultLine()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, 0, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Viashino Runner");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Nonlethal damage is marked on creatures controlled by either player")
    void marksNonlethalDamageOnBothSides() {
        var ownCreature = harness.addToBattlefieldAndReturn(player1, new ViashinoRunner());
        var opposingCreature = harness.addToBattlefieldAndReturn(player2, new ViashinoRunner());
        harness.setHand(player1, List.of(new FaultLine()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, 1, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Viashino Runner");
        harness.assertOnBattlefield(player2, "Viashino Runner");
        assertThat(ownCreature.getMarkedDamage()).isEqualTo(1);
        assertThat(opposingCreature.getMarkedDamage()).isEqualTo(1);
        harness.assertLife(player1, 19);
        harness.assertLife(player2, 19);
        harness.assertInGraveyard(player1, "Fault Line");
    }

    @Test
    @DisplayName("Flying creatures take no damage even when X exceeds their toughness")
    void excludesFlyingCreaturesFromDamage() {
        var ownFlyer = harness.addToBattlefieldAndReturn(player1, new Zephid());
        var opposingFlyer = harness.addToBattlefieldAndReturn(player2, new Zephid());
        harness.setHand(player1, List.of(new FaultLine()));
        harness.addMana(player1, ManaColor.RED, 7);

        harness.castInstant(player1, 0, 5, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Zephid");
        harness.assertOnBattlefield(player2, "Zephid");
        assertThat(ownFlyer.getMarkedDamage()).isZero();
        assertThat(opposingFlyer.getMarkedDamage()).isZero();
        harness.assertLife(player1, 15);
        harness.assertLife(player2, 15);
    }

    @Test
    @DisplayName("Damages each player with no creatures on the battlefield")
    void damagesPlayersOnEmptyBattlefield() {
        harness.setHand(player1, List.of(new FaultLine()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castInstant(player1, 0, 3, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 17);
        harness.assertInGraveyard(player1, "Fault Line");
    }
}
