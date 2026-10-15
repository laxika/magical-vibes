package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.CoralhelmGuide;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.r.RuinProcessor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DemonsGrasp.class, RuinProcessor.class, CoralhelmGuide.class, Forest.class})
class DemonsGraspTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature gets -5/-5 until end of turn")
    void targetCreatureGetsMinusFiveMinusFive() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RuinProcessor());

        castDemonsGrasp(target);

        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("The targeted creature's -5/-5 wears off at end of turn")
    void minusFiveMinusFiveWearsOffAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RuinProcessor());

        castDemonsGrasp(target);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(7);
        assertThat(target.getEffectiveToughness()).isEqualTo(8);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new DemonsGrasp()));
        addDemonsGraspMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("A creature with negative toughness dies when the spell resolves")
    void negativeToughnessKillsCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CoralhelmGuide());

        castDemonsGrasp(target);

        harness.assertNotOnBattlefield(player2, "Coralhelm Guide");
        harness.assertInGraveyard(player2, "Coralhelm Guide");
        harness.assertInGraveyard(player1, "Demon's Grasp");
    }

    @Test
    @DisplayName("Can target a creature controlled by the caster without affecting other creatures")
    void canTargetOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new RuinProcessor());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new RuinProcessor());

        castDemonsGrasp(target);

        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(3);
        assertThat(other.getEffectivePower()).isEqualTo(7);
        assertThat(other.getEffectiveToughness()).isEqualTo(8);
    }

    @Test
    @DisplayName("Repeated casts stack their toughness reductions")
    void repeatedCastsKillLargerCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RuinProcessor());

        castDemonsGrasp(target);
        harness.assertOnBattlefield(player2, "Ruin Processor");
        castDemonsGrasp(target);

        harness.assertNotOnBattlefield(player2, "Ruin Processor");
        harness.assertInGraveyard(player2, "Ruin Processor");
    }

    private void castDemonsGrasp(Permanent target) {
        harness.setHand(player1, List.of(new DemonsGrasp()));
        addDemonsGraspMana();
        harness.castAndResolveSorcery(player1, 0, target.getId());
    }

    private void addDemonsGraspMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
