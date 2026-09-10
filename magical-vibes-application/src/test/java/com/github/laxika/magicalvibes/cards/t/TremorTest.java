package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GoldenBear;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.cards.w.WildGriffin;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Tremor.class, RagingGoblin.class, WildGriffin.class, GoldenBear.class, Mountain.class})
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
        harness.addToBattlefield(player2, new WildGriffin());
        harness.addToBattlefield(player2, new RagingGoblin());
        harness.castFromHand(player1, new Tremor(), "{R}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Wild Griffin");
        harness.assertNotOnBattlefield(player2, "Raging Goblin");
    }

    @Test
    @DisplayName("Does not damage flyers controlled by either player")
    void doesNotDamageFlyersControlledByEitherPlayer() {
        Permanent ownGriffin = harness.addToBattlefieldAndReturn(player1, new WildGriffin());
        Permanent opposingGriffin = harness.addToBattlefieldAndReturn(player2, new WildGriffin());
        harness.castFromHand(player1, new Tremor(), "{R}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Wild Griffin");
        harness.assertOnBattlefield(player2, "Wild Griffin");
        assertThat(ownGriffin.getMarkedDamage()).isZero();
        assertThat(opposingGriffin.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Does not damage noncreature permanents")
    void doesNotDamageNoncreaturePermanents() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.addToBattlefield(player2, new WildGriffin());
        harness.addToBattlefield(player2, new RagingGoblin());
        harness.castFromHand(player1, new Tremor(), "{R}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Mountain");
        harness.assertOnBattlefield(player2, "Wild Griffin");
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
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GoldenBear());
        harness.castFromHand(player1, new Tremor(), "{R}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Golden Bear");
        assertThat(bears.getMarkedDamage()).isEqualTo(1);
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
