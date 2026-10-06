package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MercilessEternal.class})
class MercilessEternalTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {2}{B} and discarding a card pumps this creature +2/+2 until end of turn")
    void activationPumpsByTwo() {
        Permanent eternal = harness.addToBattlefieldAndReturn(player1, new MercilessEternal());
        int basePower = gqs.getEffectivePower(gd, eternal);
        int baseToughness = gqs.getEffectiveToughness(gd, eternal);
        harness.setHand(player1, List.of(new MercilessEternal()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Merciless Eternal");
        assertThat(gqs.getEffectivePower(gd, eternal)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, eternal)).isEqualTo(baseToughness + 2);
    }

    @Test
    @DisplayName("The +2/+2 wears off at end of turn cleanup")
    void boostWearsOffAtEndOfTurn() {
        Permanent eternal = harness.addToBattlefieldAndReturn(player1, new MercilessEternal());
        harness.setHand(player1, List.of(new MercilessEternal()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        assertThat(eternal.getPowerModifier()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(eternal.getPowerModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot activate with no card to discard")
    void cannotActivateWithoutCardInHand() {
        harness.addToBattlefieldAndReturn(player1, new MercilessEternal());
        harness.setHand(player1, new ArrayList<>());
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Afflict 2: becoming blocked makes the defending player lose 2 life")
    void blockedAfflictsDefender() {
        Permanent atk = addCreatureReady(player1, new MercilessEternal());
        atk.setAttacking(true);
        addCreatureReady(player2, new MercilessEternal());

        harness.setHand(player1, new ArrayList<>());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        // Afflict is not a drain: the defender loses 2, the attacking player's life is unchanged.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    void discardIsPaidBeforeTheBoostResolves() {
        Permanent eternal = harness.addToBattlefieldAndReturn(player1, new MercilessEternal());
        int basePower = gqs.getEffectivePower(gd, eternal);
        int baseToughness = gqs.getEffectiveToughness(gd, eternal);
        harness.setHand(player1, List.of(new MercilessEternal()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Merciless Eternal");
        assertThat(gqs.getEffectivePower(gd, eternal)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, eternal)).isEqualTo(baseToughness);

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, eternal)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, eternal)).isEqualTo(baseToughness + 2);
    }

    @Test
    void repeatedActivationsStackWhileTappedAndSummoningSick() {
        Permanent eternal = harness.addToBattlefieldAndReturn(player1, new MercilessEternal());
        eternal.tap();
        eternal.setSummoningSick(true);
        int basePower = gqs.getEffectivePower(gd, eternal);
        int baseToughness = gqs.getEffectiveToughness(gd, eternal);
        harness.setHand(player1, List.of(new MercilessEternal(), new MercilessEternal()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        for (int i = 0; i < 2; i++) {
            harness.activateAbility(player1, 0, null, null);
            harness.handleCardChosen(player1, 0);
            harness.passBothPriorities();
        }

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gqs.getEffectivePower(gd, eternal)).isEqualTo(basePower + 4);
        assertThat(gqs.getEffectiveToughness(gd, eternal)).isEqualTo(baseToughness + 4);
        assertThat(eternal.isTapped()).isTrue();
    }

    @Test
    void cannotActivateWithoutBlackMana() {
        harness.addToBattlefield(player1, new MercilessEternal());
        harness.setHand(player1, List.of(new MercilessEternal()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void multipleBlockersCauseOnlyOneAfflictTrigger() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new MercilessEternal());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.addToBattlefield(player2, new MercilessEternal());
        harness.addToBattlefield(player2, new MercilessEternal());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        harness.assertLife(player2, 20);
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertLife(player1, 20);
    }
}
