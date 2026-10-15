package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SquallLine.class, SuntailHawk.class, AirElemental.class, GrizzlyBears.class, GiantSpider.class})
class SquallLineTest extends BaseCardTest {

    private void castSquallLine(int x) {
        harness.setHand(player1, List.of(new SquallLine()));
        harness.addMana(player1, ManaColor.GREEN, x + 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castInstant(player1, 0, x, null);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Deals X damage to each player")
    void dealsDamageToEachPlayer() {
        castSquallLine(3);

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Deals X damage to flying creatures")
    void damagesFlyingCreatures() {
        harness.addToBattlefield(player2, new AirElemental());

        castSquallLine(2);

        Permanent flyer = findPermanent(player2, "Air Elemental");
        assertThat(flyer.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not damage creatures without flying")
    void ignoresNonFlyingCreatures() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GiantSpider());

        castSquallLine(3);

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Grizzly Bears")
                        || p.getCard().getName().equals("Giant Spider"))
                .allSatisfy(p -> assertThat(p.getMarkedDamage()).isEqualTo(0));
    }

    @Test
    @DisplayName("Kills a flying creature when X is lethal")
    void killsLethalFlyingCreature() {
        harness.addToBattlefield(player2, new SuntailHawk());

        castSquallLine(1);

        harness.assertNotOnBattlefield(player2, "Suntail Hawk");
    }

    @Test
    @DisplayName("X zero deals no damage to players or flying creatures")
    void zeroDealsNoDamage() {
        harness.addToBattlefield(player1, new SuntailHawk());
        harness.addToBattlefield(player2, new AirElemental());

        castSquallLine(0);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(findPermanent(player1, "Suntail Hawk").getMarkedDamage()).isZero();
        assertThat(findPermanent(player2, "Air Elemental").getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Squall Line");
    }

    @Test
    @DisplayName("Damages every flyer on both sides while leaving ground creatures unharmed")
    void damagesAllFlyersRegardlessOfController() {
        harness.addToBattlefield(player1, new SuntailHawk());
        harness.addToBattlefield(player1, new AirElemental());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new SuntailHawk());
        harness.addToBattlefield(player2, new AirElemental());
        harness.addToBattlefield(player2, new GiantSpider());

        castSquallLine(2);

        harness.assertInGraveyard(player1, "Suntail Hawk");
        harness.assertInGraveyard(player2, "Suntail Hawk");
        assertThat(findPermanent(player1, "Air Elemental").getMarkedDamage()).isEqualTo(2);
        assertThat(findPermanent(player2, "Air Elemental").getMarkedDamage()).isEqualTo(2);
        assertThat(findPermanent(player1, "Grizzly Bears").getMarkedDamage()).isZero();
        assertThat(findPermanent(player2, "Giant Spider").getMarkedDamage()).isZero();
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
    }
}
