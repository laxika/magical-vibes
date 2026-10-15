package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.d.DrudgeBeetle;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.u.UltimatePrice;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WildBeastmaster.class, DrudgeBeetle.class, GiantGrowth.class, UltimatePrice.class})
class WildBeastmasterTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking boosts other creatures you control by the Beastmaster's power")
    void attackBoostsOtherOwnCreatures() {
        Permanent beastmaster = addCreatureReady(player1, new WildBeastmaster());
        Permanent bears = addCreatureReady(player1, new DrudgeBeetle());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, beastmaster)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, beastmaster)).isEqualTo(1);
    }

    @Test
    @DisplayName("X scales with the Beastmaster's current power and skips opponents' creatures")
    void boostScalesWithPowerAndSkipsOpponents() {
        Permanent beastmaster = addCreatureReady(player1, new WildBeastmaster());
        Permanent bears = addCreatureReady(player1, new DrudgeBeetle());
        Permanent enemyBears = addCreatureReady(player2, new DrudgeBeetle());
        beastmaster.setPowerModifier(2);

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, enemyBears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOff() {
        addCreatureReady(player1, new WildBeastmaster());
        Permanent bears = addCreatureReady(player1, new DrudgeBeetle());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    void powerIsDeterminedAtResolution() {
        Permanent beastmaster = addCreatureReady(player1, new WildBeastmaster());
        Permanent beetle = addCreatureReady(player1, new DrudgeBeetle());
        declareAttackers(List.of(0));

        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, beastmaster.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, beetle)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, beetle)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, beastmaster)).isEqualTo(4);
    }

    @Test
    void removedSourceUsesItsPowerImmediatelyBeforeLeaving() {
        Permanent beastmaster = addCreatureReady(player1, new WildBeastmaster());
        Permanent beetle = addCreatureReady(player1, new DrudgeBeetle());
        declareAttackers(List.of(0));

        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, beastmaster.getId());
        harness.setHand(player1, List.of(new UltimatePrice()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, beastmaster.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Wild Beastmaster");
        assertThat(gqs.getEffectivePower(gd, beetle)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, beetle)).isEqualTo(6);
    }

    @Test
    void zeroPowerGivesNoBonus() {
        Permanent beastmaster = addCreatureReady(player1, new WildBeastmaster());
        Permanent beetle = addCreatureReady(player1, new DrudgeBeetle());
        beastmaster.setPowerModifier(-1);
        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, beetle)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, beetle)).isEqualTo(2);
    }

    @Test
    void negativePowerUsesZeroForX() {
        Permanent beastmaster = addCreatureReady(player1, new WildBeastmaster());
        Permanent beetle = addCreatureReady(player1, new DrudgeBeetle());
        beastmaster.setPowerModifier(-3);
        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, beetle)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, beetle)).isEqualTo(2);
    }
}
