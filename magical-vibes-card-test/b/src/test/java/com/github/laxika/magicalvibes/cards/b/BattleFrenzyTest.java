package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.k.KjeldoranFrostbeast;
import com.github.laxika.magicalvibes.cards.w.WallOfShields;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BattleFrenzy.class, BalduvianBears.class, BalduvianBarbarians.class,
        KjeldoranFrostbeast.class, WallOfShields.class})
class BattleFrenzyTest extends BaseCardTest {

    private void castBattleFrenzy() {
        harness.castFromHand(player1, new BattleFrenzy(), "{2}{R}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Green creatures you control get +1/+1, nongreen get +1/+0")
    void boostsByColor() {
        harness.addToBattlefield(player1, new BalduvianBears());
        harness.addToBattlefield(player1, new BalduvianBarbarians());

        castBattleFrenzy();

        Permanent bears = findPermanent(player1, "Balduvian Bears");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);

        Permanent barbarians = findPermanent(player1, "Balduvian Barbarians");
        assertThat(gqs.getEffectivePower(gd, barbarians)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, barbarians)).isEqualTo(2);
    }

    @Test
    @DisplayName("Counts multicolored green creatures as green and colorless creatures as nongreen")
    void handlesMulticoloredAndColorlessCreatures() {
        harness.addToBattlefield(player1, new KjeldoranFrostbeast());
        harness.addToBattlefield(player1, new WallOfShields());

        castBattleFrenzy();

        Permanent frostbeast = findPermanent(player1, "Kjeldoran Frostbeast");
        assertThat(gqs.getEffectivePower(gd, frostbeast)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, frostbeast)).isEqualTo(5);

        Permanent wall = findPermanent(player1, "Wall of Shields");
        assertThat(gqs.getEffectivePower(gd, wall)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, wall)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not boost opponent's creatures")
    void doesNotBoostOpponentCreatures() {
        harness.addToBattlefield(player2, new BalduvianBears());
        harness.addToBattlefield(player2, new BalduvianBarbarians());

        castBattleFrenzy();

        assertThat(gqs.getEffectivePower(gd, findPermanent(player2, "Balduvian Bears"))).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, findPermanent(player2, "Balduvian Barbarians"))).isEqualTo(3);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOff() {
        harness.addToBattlefield(player1, new BalduvianBears());
        harness.addToBattlefield(player1, new BalduvianBarbarians());

        castBattleFrenzy();

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, findPermanent(player1, "Balduvian Bears"))).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, findPermanent(player1, "Balduvian Bears"))).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, findPermanent(player1, "Balduvian Barbarians"))).isEqualTo(3);
    }

    @Test
    @DisplayName("Affects creatures present at resolution, but not creatures entering afterward")
    void locksInCreaturesAtResolution() {
        harness.castFromHand(player1, new BattleFrenzy(), "{2}{R}");
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new BalduvianBears());
        Permanent barbarians = harness.addToBattlefieldAndReturn(player1, new BalduvianBarbarians());

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, barbarians)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, barbarians)).isEqualTo(2);

        Permanent laterBears = harness.addToBattlefieldAndReturn(player1, new BalduvianBears());
        Permanent laterBarbarians = harness.addToBattlefieldAndReturn(player1, new BalduvianBarbarians());

        assertThat(gqs.getEffectivePower(gd, laterBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, laterBears)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, laterBarbarians)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, laterBarbarians)).isEqualTo(2);
    }

    @Test
    @DisplayName("Multiple Battle Frenzies give cumulative bonuses")
    void multipleCastsStack() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new BalduvianBears());
        Permanent barbarians = harness.addToBattlefieldAndReturn(player1, new BalduvianBarbarians());

        castBattleFrenzy();
        castBattleFrenzy();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, barbarians)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, barbarians)).isEqualTo(2);
    }
}
