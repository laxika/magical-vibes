package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GlacialWall;
import com.github.laxika.magicalvibes.cards.g.GoblinHero;
import com.github.laxika.magicalvibes.cards.h.HowlingMine;
import com.github.laxika.magicalvibes.cards.m.MahamotiDjinn;
import com.github.laxika.magicalvibes.cards.p.PhyrexianColossus;
import com.github.laxika.magicalvibes.cards.s.SeaMonster;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FountainOfYouth.class, GlacialWall.class, GoblinHero.class, HowlingMine.class, Inferno.class, MahamotiDjinn.class, PhyrexianColossus.class, SeaMonster.class})
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
    @DisplayName("Inferno deals lethal damage to creatures with toughness 6 or less on both sides")
    void dealsLethalDamageToCreaturesWithToughnessSixOrLess() {
        harness.addToBattlefield(player1, new MahamotiDjinn());
        harness.addToBattlefield(player2, new MahamotiDjinn());

        harness.castFromHand(player1, new Inferno(), "{5}{R}{R}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Mahamoti Djinn");
        harness.assertNotOnBattlefield(player2, "Mahamoti Djinn");
    }

    @Test
    @DisplayName("Inferno does not destroy creatures with toughness greater than 6")
    void doesNotDestroyLargeCreatures() {
        var phyrexianColossus = harness.addToBattlefieldAndReturn(player2, new PhyrexianColossus());

        harness.castFromHand(player1, new Inferno(), "{5}{R}{R}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Phyrexian Colossus");
        assertThat(phyrexianColossus.getMarkedDamage()).isEqualTo(6);
    }

    @Test
    @DisplayName("Inferno does not destroy creatures with toughness greater than 6")
    void doesNotDestroyLargeCreaturesUpstreamReview() {
        var glacialWall = harness.addToBattlefieldAndReturn(player2, new GlacialWall());

        harness.castFromHand(player1, new Inferno(), "{5}{R}{R}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Glacial Wall");
        assertThat(glacialWall.getMarkedDamage()).isEqualTo(6);
    }

    @Test
    @DisplayName("Inferno does not affect noncreature permanents")
    void doesNotAffectNoncreaturePermanents() {
        var howlingMine = harness.addToBattlefieldAndReturn(player2, new HowlingMine());

        harness.castFromHand(player1, new Inferno(), "{5}{R}{R}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Howling Mine");
        assertThat(howlingMine.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Inferno destroys creatures with toughness exactly 6")
    void destroysCreatureWithToughnessExactlySix() {
        harness.addToBattlefield(player2, new SeaMonster());

        harness.castFromHand(player1, new Inferno(), "{5}{R}{R}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Sea Monster");
    }
}
