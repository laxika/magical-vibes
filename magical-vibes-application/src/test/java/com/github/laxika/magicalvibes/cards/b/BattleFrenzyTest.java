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
        assertThat(bears.getEffectivePower()).isEqualTo(3);
        assertThat(bears.getEffectiveToughness()).isEqualTo(3);

        Permanent barbarians = findPermanent(player1, "Balduvian Barbarians");
        assertThat(barbarians.getEffectivePower()).isEqualTo(4);
        assertThat(barbarians.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Counts multicolored green creatures as green and colorless creatures as nongreen")
    void handlesMulticoloredAndColorlessCreatures() {
        harness.addToBattlefield(player1, new KjeldoranFrostbeast());
        harness.addToBattlefield(player1, new WallOfShields());

        castBattleFrenzy();

        Permanent frostbeast = findPermanent(player1, "Kjeldoran Frostbeast");
        assertThat(frostbeast.getEffectivePower()).isEqualTo(3);
        assertThat(frostbeast.getEffectiveToughness()).isEqualTo(5);

        Permanent wall = findPermanent(player1, "Wall of Shields");
        assertThat(wall.getEffectivePower()).isEqualTo(1);
        assertThat(wall.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not boost opponent's creatures")
    void doesNotBoostOpponentCreatures() {
        harness.addToBattlefield(player2, new BalduvianBears());
        harness.addToBattlefield(player2, new BalduvianBarbarians());

        castBattleFrenzy();

        assertThat(findPermanent(player2, "Balduvian Bears").getEffectivePower()).isEqualTo(2);
        assertThat(findPermanent(player2, "Balduvian Barbarians").getEffectivePower()).isEqualTo(3);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOff() {
        harness.addToBattlefield(player1, new BalduvianBears());
        harness.addToBattlefield(player1, new BalduvianBarbarians());

        castBattleFrenzy();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Balduvian Bears").getEffectivePower()).isEqualTo(2);
        assertThat(findPermanent(player1, "Balduvian Bears").getEffectiveToughness()).isEqualTo(2);
        assertThat(findPermanent(player1, "Balduvian Barbarians").getEffectivePower()).isEqualTo(3);
    }
}
