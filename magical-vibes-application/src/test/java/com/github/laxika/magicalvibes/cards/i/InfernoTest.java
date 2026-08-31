package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.d.DarkSphere;
import com.github.laxika.magicalvibes.cards.g.GoblinHero;
import com.github.laxika.magicalvibes.cards.l.Leviathan;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Inferno.class, GoblinHero.class, Leviathan.class, DarkSphere.class})
class InfernoTest extends BaseCardTest {

    @Test
    @DisplayName("Inferno deals 6 damage to each player")
    void dealsSixDamageToEachPlayer() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.castFromHand(player1, new Inferno(), "{5}{R}{R}");
        harness.passBothPriorities();

        harness.assertLife(player1, 14);
        harness.assertLife(player2, 14);
    }

    @Test
    @DisplayName("Inferno destroys creatures with toughness 6 or less on both sides")
    void destroysCreaturesWithToughnessSixOrLess() {
        harness.addToBattlefield(player1, new GoblinHero());
        harness.addToBattlefield(player2, new GoblinHero());

        harness.castFromHand(player1, new Inferno(), "{5}{R}{R}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Goblin Hero");
        harness.assertNotOnBattlefield(player2, "Goblin Hero");
    }

    @Test
    @DisplayName("Inferno does not destroy creatures with toughness greater than 6")
    void doesNotDestroyLargeCreatures() {
        var leviathan = harness.addToBattlefieldAndReturn(player2, new Leviathan());

        harness.castFromHand(player1, new Inferno(), "{5}{R}{R}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Leviathan");
        assertThat(leviathan.getMarkedDamage()).isEqualTo(6);
    }

    @Test
    @DisplayName("Inferno does not affect noncreature permanents")
    void doesNotAffectNoncreaturePermanents() {
        var darkSphere = harness.addToBattlefieldAndReturn(player2, new DarkSphere());

        harness.castFromHand(player1, new Inferno(), "{5}{R}{R}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Dark Sphere");
        assertThat(darkSphere.getMarkedDamage()).isZero();
    }
}
