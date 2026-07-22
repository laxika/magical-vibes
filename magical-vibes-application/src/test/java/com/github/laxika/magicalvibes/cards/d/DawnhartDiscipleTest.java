package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DawnhartDiscipleTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+1 until end of turn when another Human enters")
    void boostsWhenHumanEnters() {
        Permanent disciple = harness.addToBattlefieldAndReturn(player1, new DawnhartDisciple());

        harness.setHand(player1, List.of(new EliteVanguard()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve +1/+1 trigger

        assertThat(disciple.getPowerModifier()).isEqualTo(1);
        assertThat(disciple.getToughnessModifier()).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, disciple)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, disciple)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not boost when a non-Human creature enters")
    void noBoostWhenNonHumanEnters() {
        Permanent disciple = harness.addToBattlefieldAndReturn(player1, new DawnhartDisciple());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(disciple.getPowerModifier()).isEqualTo(0);
        assertThat(disciple.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Does not trigger when an opponent's Human enters")
    void noBoostWhenOpponentHumanEnters() {
        Permanent disciple = harness.addToBattlefieldAndReturn(player1, new DawnhartDisciple());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new EliteVanguard()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(disciple.getPowerModifier()).isEqualTo(0);
        assertThat(disciple.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOffAtCleanup() {
        Permanent disciple = harness.addToBattlefieldAndReturn(player1, new DawnhartDisciple());

        harness.setHand(player1, List.of(new EliteVanguard()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(disciple.getPowerModifier()).isEqualTo(1);

        harness.setHand(player1, new ArrayList<>());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(disciple.getPowerModifier()).isEqualTo(0);
        assertThat(disciple.getToughnessModifier()).isEqualTo(0);
        assertThat(gqs.getEffectivePower(gd, disciple)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, disciple)).isEqualTo(2);
    }

    @Test
    @DisplayName("Stacks multiple boosts from multiple Human entries")
    void stacksMultipleBoosts() {
        Permanent disciple = harness.addToBattlefieldAndReturn(player1, new DawnhartDisciple());

        harness.setHand(player1, List.of(new EliteVanguard()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(disciple.getPowerModifier()).isEqualTo(1);

        harness.setHand(player1, List.of(new EliteVanguard()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(disciple.getPowerModifier()).isEqualTo(2);
        assertThat(disciple.getToughnessModifier()).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, disciple)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, disciple)).isEqualTo(4);
    }
}
