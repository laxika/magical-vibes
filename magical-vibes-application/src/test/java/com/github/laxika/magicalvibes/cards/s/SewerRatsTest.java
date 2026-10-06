package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(SewerRats.class)
class SewerRatsTest extends BaseCardTest {

    @Test
    @DisplayName("Ability grants +1/+0 and costs 1 life")
    void abilityBoostsAndCostsLife() {
        Permanent rats = addCreatureReady(player1, new SewerRats());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, rats)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, rats)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Ability can be activated no more than three times each turn")
    void abilityLimitedToThreeActivations() {
        Permanent rats = addCreatureReady(player1, new SewerRats());
        harness.addMana(player1, ManaColor.BLACK, 4);

        for (int i = 0; i < 3; i++) {
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
            harness.clearPriorityPassed();
        }

        assertThat(gqs.getEffectivePower(gd, rats)).isEqualTo(4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no more than 3 times each turn");
    }

    @Test
    @DisplayName("Activation limit resets on the next turn")
    void activationLimitResetsOnNextTurn() {
        Permanent rats = addCreatureReady(player1, new SewerRats());
        harness.addMana(player1, ManaColor.BLACK, 4);

        for (int i = 0; i < 3; i++) {
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
            harness.clearPriorityPassed();
        }

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, rats)).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOff() {
        Permanent rats = addCreatureReady(player1, new SewerRats());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, rats)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, rats)).isEqualTo(1);
    }

    @Test
    @DisplayName("Costs and activation limit apply before any activation resolves")
    void pendingActivationsCountTowardLimit() {
        Permanent rats = addCreatureReady(player1, new SewerRats());
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.setLife(player1, 20);

        for (int i = 0; i < 3; i++) {
            harness.activateAbility(player1, 0, null, null);
        }

        harness.assertLife(player1, 17);
        assertThat(gqs.getEffectivePower(gd, rats)).isEqualTo(1);
        assertThat(gd.stack).hasSize(3);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no more than 3 times each turn");
        harness.assertLife(player1, 17);

        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, rats)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, rats)).isEqualTo(1);
    }

    @Test
    @DisplayName("Tapped and summoning-sick Sewer Rats can activate its ability")
    void abilityDoesNotRequireTapOrHaste() {
        Permanent rats = harness.addToBattlefieldAndReturn(player1, new SewerRats());
        rats.setSummoningSick(true);
        rats.tap();
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, rats)).isEqualTo(2);
        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("Each Sewer Rats has its own activation limit and receives only its own boost")
    void activationLimitIsPerPermanent() {
        Permanent first = addCreatureReady(player1, new SewerRats());
        Permanent second = addCreatureReady(player1, new SewerRats());
        harness.addMana(player1, ManaColor.BLACK, 6);

        for (int i = 0; i < 3; i++) {
            harness.activateAbility(player1, 0, null, null);
            harness.activateAbility(player1, 1, null, null);
        }
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        harness.assertLife(player1, 14);
    }

    @Test
    @DisplayName("An activation without black mana pays no life and does not consume the limit")
    void failedManaPaymentDoesNotConsumeActivation() {
        Permanent rats = addCreatureReady(player1, new SewerRats());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.setLife(player1, 20);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();

        harness.addMana(player1, ManaColor.BLACK, 3);
        for (int i = 0; i < 3; i++) {
            harness.activateAbility(player1, 0, null, null);
        }
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, rats)).isEqualTo(4);
        harness.assertLife(player1, 17);
    }
}
