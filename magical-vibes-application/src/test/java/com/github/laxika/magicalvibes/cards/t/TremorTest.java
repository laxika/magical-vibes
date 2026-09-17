package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.k.KyrenGlider;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.r.RamosianSergeant;
import com.github.laxika.magicalvibes.cards.w.WildJhovall;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Tremor.class, RamosianSergeant.class, KyrenGlider.class, WildJhovall.class, Mountain.class})
class TremorTest extends BaseCardTest {

    @Test
    @DisplayName("Kills ground creatures on both sides")
    void killsGroundCreatures() {
        harness.addToBattlefield(player1, new RamosianSergeant());
        harness.addToBattlefield(player2, new RamosianSergeant());
        harness.castFromHand(player1, new Tremor(), "{R}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Ramosian Sergeant");
        harness.assertNotOnBattlefield(player2, "Ramosian Sergeant");
    }

    @Test
    @DisplayName("Does not damage creatures with flying")
    void doesNotDamageFlyers() {
        harness.addToBattlefield(player2, new KyrenGlider());
        harness.addToBattlefield(player2, new RamosianSergeant());
        harness.castFromHand(player1, new Tremor(), "{R}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Kyren Glider");
        harness.assertNotOnBattlefield(player2, "Ramosian Sergeant");
    }

    @Test
    @DisplayName("Does not damage flyers controlled by either player")
    void doesNotDamageFlyersControlledByEitherPlayer() {
        Permanent ownGlider = harness.addToBattlefieldAndReturn(player1, new KyrenGlider());
        Permanent opposingGlider = harness.addToBattlefieldAndReturn(player2, new KyrenGlider());
        harness.castFromHand(player1, new Tremor(), "{R}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Kyren Glider");
        harness.assertOnBattlefield(player2, "Kyren Glider");
        assertThat(ownGlider.getMarkedDamage()).isZero();
        assertThat(opposingGlider.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Does not damage noncreature permanents")
    void doesNotDamageNoncreaturePermanents() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.addToBattlefield(player2, new KyrenGlider());
        harness.addToBattlefield(player2, new RamosianSergeant());
        harness.castFromHand(player1, new Tremor(), "{R}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Mountain");
        harness.assertOnBattlefield(player2, "Kyren Glider");
        harness.assertNotOnBattlefield(player2, "Ramosian Sergeant");
        assertThat(mountain.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Does not deal damage to players")
    void doesNotDamagePlayers() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.castFromHand(player1, new Tremor(), "{R}");
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Deals exactly 1 damage to a larger ground creature")
    void dealsOneDamageToLargerGroundCreature() {
        Permanent jhovall = harness.addToBattlefieldAndReturn(player2, new WildJhovall());
        harness.castFromHand(player1, new Tremor(), "{R}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Wild Jhovall");
        assertThat(jhovall.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Goes to graveyard after resolving")
    void goesToGraveyard() {
        harness.castFromHand(player1, new Tremor(), "{R}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Tremor");
    }
}
