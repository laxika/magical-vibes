package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AirElemental.class, HillGiant.class, Mountain.class, RagingGoblin.class, Tremor.class})
class TremorTest extends BaseCardTest {

    @Test
    @DisplayName("Kills ground creatures on both sides")
    void killsGroundCreatures() {
        harness.addToBattlefield(player1, new RagingGoblin());
        harness.addToBattlefield(player2, new RagingGoblin());
        harness.castFromHand(player1, new Tremor(), "{R}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Raging Goblin");
        harness.assertNotOnBattlefield(player2, "Raging Goblin");
    }

    @Test
    @DisplayName("Does not damage creatures with flying")
    void doesNotDamageFlyers() {
        harness.addToBattlefield(player2, new AirElemental());
        harness.addToBattlefield(player2, new RagingGoblin());
        harness.castFromHand(player1, new Tremor(), "{R}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Air Elemental");
        harness.assertNotOnBattlefield(player2, "Raging Goblin");
    }

    @Test
    @DisplayName("Does not damage flyers controlled by either player")
    void doesNotDamageFlyersControlledByEitherPlayer() {
        Permanent ownElemental = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        Permanent opposingElemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.castFromHand(player1, new Tremor(), "{R}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Air Elemental");
        harness.assertOnBattlefield(player2, "Air Elemental");
        assertThat(ownElemental.getMarkedDamage()).isZero();
        assertThat(opposingElemental.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Does not damage noncreature permanents")
    void doesNotDamageNoncreaturePermanents() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.addToBattlefield(player2, new AirElemental());
        harness.addToBattlefield(player2, new RagingGoblin());
        harness.castFromHand(player1, new Tremor(), "{R}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Mountain");
        harness.assertOnBattlefield(player2, "Air Elemental");
        harness.assertNotOnBattlefield(player2, "Raging Goblin");
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
        Permanent hillGiant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.castFromHand(player1, new Tremor(), "{R}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Hill Giant");
        assertThat(hillGiant.getMarkedDamage()).isEqualTo(1);
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
