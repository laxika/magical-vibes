package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.b.BeastriderVanguard;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VeteranBeastrider.class, BeastriderVanguard.class, Island.class})
class VeteranBeastriderTest extends BaseCardTest {

    private void advanceToEndStepTrigger() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();
    }

    @Test
    @DisplayName("Untaps each creature you control at the beginning of your end step")
    void untapsControlledCreatures() {
        harness.addToBattlefield(player1, new VeteranBeastrider());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new BeastriderVanguard());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new BeastriderVanguard());
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new Island());
        ownCreature.tap();
        opponentCreature.tap();
        ownLand.tap();

        advanceToEndStepTrigger();

        assertThat(ownCreature.isTapped()).isFalse();
        assertThat(opponentCreature.isTapped()).isTrue();
        assertThat(ownLand.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Paying {2}{G}{W} boosts creatures you control until end of turn")
    void boostsOwnCreaturesUntilEndOfTurn() {
        harness.addToBattlefield(player1, new VeteranBeastrider());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new BeastriderVanguard());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new BeastriderVanguard());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not untap creatures during the opponent's end step")
    void doesNotUntapOnOpponentsEndStep() {
        Permanent rider = harness.addToBattlefieldAndReturn(player1, new VeteranBeastrider());
        rider.tap();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(rider.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The end-step trigger untaps its source and creatures present at resolution")
    void untapsCreaturesPresentAtResolutionAfterSourceLeaves() {
        Permanent rider = harness.addToBattlefieldAndReturn(player1, new VeteranBeastrider());
        rider.tap();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(rider.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);

        gd.playerBattlefields.get(player1.getId()).remove(rider);
        Permanent newcomer = harness.addToBattlefieldAndReturn(player1, new BeastriderVanguard());
        newcomer.tap();
        resolveAllTriggers();

        assertThat(newcomer.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Repeated activations boost the source and stack without boosting later arrivals")
    void boostsStackAndExcludeLaterArrivals() {
        Permanent rider = harness.addToBattlefieldAndReturn(player1, new VeteranBeastrider());
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, rider)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, rider)).isEqualTo(5);

        Permanent newcomer = harness.addToBattlefieldAndReturn(player1, new BeastriderVanguard());
        assertThat(gqs.getEffectivePower(gd, newcomer)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, newcomer)).isEqualTo(2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, rider)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, rider)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, newcomer)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, newcomer)).isEqualTo(3);
    }

    @Test
    @DisplayName("The end-step trigger untaps Veteran Beastrider itself")
    void untapsItself() {
        Permanent rider = harness.addToBattlefieldAndReturn(player1, new VeteranBeastrider());
        rider.tap();

        advanceToEndStepTrigger();

        assertThat(rider.isTapped()).isFalse();
    }
}
