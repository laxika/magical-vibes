package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.Abrade;
import com.github.laxika.magicalvibes.cards.f.FirebrandArcher;
import com.github.laxika.magicalvibes.cards.g.GraniticTitan;
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

@CardUsed({BurningFistMinotaur.class, GraniticTitan.class, Abrade.class, FirebrandArcher.class})
class BurningFistMinotaurTest extends BaseCardTest {

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    @Test
    @DisplayName("{1}{R}, Discard a card gives this creature +2/+0 until end of turn")
    void discardBoostsPlusTwoZero() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent minotaur = harness.addToBattlefieldAndReturn(player1, new BurningFistMinotaur());
        int basePower = gqs.getEffectivePower(gd, minotaur);
        int baseToughness = gqs.getEffectiveToughness(gd, minotaur);
        harness.setHand(player1, List.of(new GraniticTitan()));
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0); // pay the discard cost
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Granitic Titan");
        assertThat(gqs.getEffectivePower(gd, minotaur)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, minotaur)).isEqualTo(baseToughness);
    }

    @Test
    @DisplayName("The +2/+0 boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent minotaur = harness.addToBattlefieldAndReturn(player1, new BurningFistMinotaur());
        int basePower = gqs.getEffectivePower(gd, minotaur);
        harness.setHand(player1, List.of(new GraniticTitan()));
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, minotaur)).isEqualTo(basePower + 2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, minotaur)).isEqualTo(basePower);
    }

    @Test
    @DisplayName("Cannot activate with no card to discard")
    void cannotActivateWithoutCardInHand() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addToBattlefieldAndReturn(player1, new BurningFistMinotaur());
        harness.setHand(player1, new ArrayList<>());
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Repeated activations each discard a card and their boosts add together")
    void repeatedActivationsStack() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent minotaur = harness.addToBattlefieldAndReturn(player1, new BurningFistMinotaur());
        int basePower = gqs.getEffectivePower(gd, minotaur);
        int baseToughness = gqs.getEffectiveToughness(gd, minotaur);
        harness.setHand(player1, List.of(new GraniticTitan(), new Abrade()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Granitic Titan");
        harness.assertInGraveyard(player1, "Abrade");
        assertThat(gqs.getEffectivePower(gd, minotaur)).isEqualTo(basePower);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, minotaur)).isEqualTo(basePower + 4);
        assertThat(gqs.getEffectiveToughness(gd, minotaur)).isEqualTo(baseToughness);
    }

    @Test
    @DisplayName("A tapped, summoning-sick Minotaur can activate on the opponent's turn")
    void canActivateOnOpponentsTurnWhileTapped() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent minotaur = harness.addToBattlefieldAndReturn(player1, new BurningFistMinotaur());
        minotaur.tap();
        minotaur.setSummoningSick(true);
        int basePower = gqs.getEffectivePower(gd, minotaur);
        harness.setHand(player1, List.of(new Abrade()));
        addActivationMana();
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Abrade");
        assertThat(gqs.getEffectivePower(gd, minotaur)).isEqualTo(basePower);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, minotaur)).isEqualTo(basePower + 2);
        assertThat(minotaur.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot pay the activation's red mana requirement with only colorless mana")
    void cannotActivateWithoutRedMana() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent minotaur = harness.addToBattlefieldAndReturn(player1, new BurningFistMinotaur());
        int basePower = gqs.getEffectivePower(gd, minotaur);
        Abrade discard = new Abrade();
        harness.setHand(player1, List.of(discard));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(discard);
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, minotaur)).isEqualTo(basePower);
    }

    @Test
    @DisplayName("First strike kills a blocker before it can deal combat damage")
    void firstStrikeKillsBlockerBeforeRegularDamage() {
        addCreatureReady(player1, new BurningFistMinotaur());
        addCreatureReady(player2, new FirebrandArcher());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertOnBattlefield(player1, "Burning-Fist Minotaur");
        harness.assertInGraveyard(player2, "Firebrand Archer");
        harness.assertLife(player2, 20);
    }
}
