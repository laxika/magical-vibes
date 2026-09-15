package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RazorfootGriffin;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Squall.class, AirElemental.class, GiantSpider.class, GrizzlyBears.class,
        RazorfootGriffin.class, SkyshroudFalcon.class})
class SquallTest extends BaseCardTest {

    private void castSquall() {
        harness.castFromHand(player1, new Squall(), "{2}{G}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Squall destroys a flying creature with 2 or less toughness")
    void destroysSmallFlyer() {
        harness.addToBattlefield(player2, new RazorfootGriffin());

        castSquall();

        harness.assertNotOnBattlefield(player2, "Razorfoot Griffin");
    }

    @Test
    @DisplayName("Squall destroys small flyers controlled by both players")
    void destroysSmallFlyersControlledByBothPlayers() {
        harness.addToBattlefield(player1, new SkyshroudFalcon());
        harness.addToBattlefield(player2, new SkyshroudFalcon());

        castSquall();

        harness.assertNotOnBattlefield(player1, "Skyshroud Falcon");
        harness.assertNotOnBattlefield(player2, "Skyshroud Falcon");
    }

    @Test
    @DisplayName("Squall marks 2 damage on a larger flyer that survives")
    void damagesLargeFlyer() {
        harness.addToBattlefield(player2, new AirElemental());

        castSquall();

        Permanent flyer = findPermanent(player2, "Air Elemental");
        assertThat(flyer.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Squall damages flying creatures controlled by either player")
    void damagesFlyingCreaturesControlledByBothPlayers() {
        harness.addToBattlefield(player1, new AirElemental());
        harness.addToBattlefield(player2, new AirElemental());

        castSquall();

        assertThat(findPermanent(player1, "Air Elemental").getMarkedDamage()).isEqualTo(2);
        assertThat(findPermanent(player2, "Air Elemental").getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Squall does not damage non-flying creatures, including reach")
    void ignoresNonFlyers() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GiantSpider());

        castSquall();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Grizzly Bears")
                        || p.getCard().getName().equals("Giant Spider"))
                .allSatisfy(p -> assertThat(p.getMarkedDamage()).isEqualTo(0));
    }

    @Test
    @DisplayName("Squall does not deal damage to players")
    void doesNotDamagePlayers() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        castSquall();

        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}
