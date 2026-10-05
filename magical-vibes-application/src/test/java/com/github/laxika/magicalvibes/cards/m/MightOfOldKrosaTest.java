package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.cards.l.LocketOfYesterdays;
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

@CardUsed({MightOfOldKrosa.class, AshcoatBear.class, LocketOfYesterdays.class})
class MightOfOldKrosaTest extends BaseCardTest {

    @Test
    @DisplayName("Gives the target creature +4/+4 when you cast it during your main phase")
    void givesPlusFourPlusFourDuringMainPhase() {
        Permanent target = addCreatureReady(player2, new AshcoatBear());
        harness.setHand(player1, List.of(new MightOfOldKrosa()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getPowerModifier()).isEqualTo(4);
        assertThat(target.getToughnessModifier()).isEqualTo(4);
    }

    @Test
    @DisplayName("Gives the target creature +2/+2 when cast outside your main phase")
    void givesPlusTwoPlusTwoOutsideMainPhase() {
        Permanent target = addCreatureReady(player2, new AshcoatBear());
        harness.setHand(player1, List.of(new MightOfOldKrosa()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Gives the target creature +4/+4 when cast during the postcombat main phase")
    void givesPlusFourPlusFourDuringPostcombatMainPhase() {
        Permanent target = addCreatureReady(player2, new AshcoatBear());
        harness.setHand(player1, List.of(new MightOfOldKrosa()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getPowerModifier()).isEqualTo(4);
        assertThat(target.getToughnessModifier()).isEqualTo(4);
    }

    @Test
    @DisplayName("Gives the target creature only +2/+2 during an opponent's main phase")
    void givesPlusTwoPlusTwoDuringOpponentsMainPhase() {
        Permanent target = addCreatureReady(player2, new AshcoatBear());
        harness.setHand(player1, List.of(new MightOfOldKrosa()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("The boost wears off at the end of the turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent target = addCreatureReady(player2, new AshcoatBear());
        harness.setHand(player1, List.of(new MightOfOldKrosa()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveInstant(player1, 0, target.getId());
        assertThat(target.getPowerModifier()).isEqualTo(4);
        assertThat(target.getToughnessModifier()).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Main-phase boosts accumulate even when cast with another spell on the stack")
    void mainPhaseBoostsAccumulateWithNonemptyStack() {
        Permanent target = addCreatureReady(player1, new AshcoatBear());
        harness.setHand(player1, List.of(new MightOfOldKrosa(), new MightOfOldKrosa()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castInstant(player1, 0, target.getId());
        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getPowerModifier()).isEqualTo(4);
        assertThat(target.getToughnessModifier()).isEqualTo(4);

        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(8);
        assertThat(target.getToughnessModifier()).isEqualTo(8);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LocketOfYesterdays());
        harness.setHand(player1, List.of(new MightOfOldKrosa()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
