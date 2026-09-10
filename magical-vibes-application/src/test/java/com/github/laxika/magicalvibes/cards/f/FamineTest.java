package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.s.SouthernElephant;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Famine.class, ForestBear.class, SouthernElephant.class, Forest.class})
class FamineTest extends BaseCardTest {

    @Test
    @DisplayName("Famine deals 3 damage to each creature and each player")
    void dealsDamageToCreaturesAndPlayers() {
        harness.addToBattlefield(player1, new ForestBear()); // 2/2
        harness.addToBattlefield(player2, new ForestBear()); // 2/2
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castFromHand(player1, new Famine(), "{3}{B}{B}");
        harness.passBothPriorities();

        // Both 2/2 creatures die to 3 damage
        harness.assertNotOnBattlefield(player1, "Forest Bear");
        harness.assertNotOnBattlefield(player2, "Forest Bear");
        // Both players take 3 damage
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Famine does not kill creatures with toughness greater than 3")
    void doesNotKillToughCreatures() {
        Permanent elephant = harness.addToBattlefieldAndReturn(player2, new SouthernElephant()); // 3/4

        harness.castFromHand(player1, new Famine(), "{3}{B}{B}");
        harness.passBothPriorities();

        assertThat(elephant.getMarkedDamage()).isEqualTo(3);
        harness.assertOnBattlefield(player2, "Southern Elephant");
    }

    @Test
    @DisplayName("Famine goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.castFromHand(player1, new Famine(), "{3}{B}{B}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Famine");
    }

    @Test
    @DisplayName("Famine does not damage noncreature permanents")
    void doesNotDamageNoncreaturePermanents() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.castFromHand(player1, new Famine(), "{3}{B}{B}");
        harness.passBothPriorities();

        assertThat(forest.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Forest");
    }
}
