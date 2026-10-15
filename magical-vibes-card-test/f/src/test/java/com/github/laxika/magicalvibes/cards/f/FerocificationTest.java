package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Ferocification.class, GrizzlyBears.class})
class FerocificationTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts a chosen creature you control by +2/+0")
    void boostsChosenCreature() {
        harness.addToBattlefield(player1, new Ferocification());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToBeginningOfCombat(player1);
        harness.handleListChoice(player1, "Target creature you control gets +2/+0 until end of turn");
        harness.handlePermanentChosen(player1, bear.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
    }

    @Test
    @DisplayName("Grants menace and haste to a chosen creature you control")
    void grantsMenaceAndHaste() {
        harness.addToBattlefield(player1, new Ferocification());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToBeginningOfCombat(player1);
        harness.handleListChoice(player1, "Target creature you control gains menace and haste until end of turn");
        harness.handlePermanentChosen(player1, bear.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bear, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Only permits creatures you control as targets")
    void onlyTargetsCreaturesYouControl() {
        harness.addToBattlefield(player1, new Ferocification());
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToBeginningOfCombat(player1);
        harness.handleListChoice(player1, "Target creature you control gets +2/+0 until end of turn");

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opposingBear.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.handlePermanentChosen(player1, ownBear.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownBear)).isEqualTo(4);
    }

    @Test
    @DisplayName("The temporary mode effect wears off at end of turn")
    void effectWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new Ferocification());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToBeginningOfCombat(player1);
        harness.handleListChoice(player1, "Target creature you control gets +2/+0 until end of turn");
        harness.handlePermanentChosen(player1, bear.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(4);

        gd.interaction.clearAwaitingInput();
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
    }

    @Test
    @DisplayName("Menace and haste expire at end of turn")
    void keywordsWearOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new Ferocification());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToBeginningOfCombat(player1);
        harness.handleListChoice(player1, "Target creature you control gains menace and haste until end of turn");
        harness.handlePermanentChosen(player1, bear.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.HASTE)).isTrue();

        gd.interaction.clearAwaitingInput();
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, bear, Keyword.MENACE)).isFalse();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Does not trigger during an opponent's combat")
    void doesNotTriggerOnOpponentsTurn() {
        harness.addToBattlefield(player1, new Ferocification());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToBeginningOfCombat(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.MENACE)).isFalse();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("No ability is put on the stack when neither mode has a legal target")
    void noLegalTarget() {
        harness.addToBattlefield(player1, new Ferocification());
        harness.addToBattlefield(player2, new GrizzlyBears());

        advanceToBeginningOfCombat(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "Target creature you control gets +2/+0 until end of turn",
            "Target creature you control gains menace and haste until end of turn"
    })
    @DisplayName("Both modes fail to resolve if the target changes controller")
    void targetMustStillBeControlledOnResolution(String mode) {
        harness.addToBattlefield(player1, new Ferocification());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToBeginningOfCombat(player1);
        harness.handleListChoice(player1, mode);
        harness.handlePermanentChosen(player1, bear.getId());
        gd.playerBattlefields.get(player1.getId()).remove(bear);
        gd.playerBattlefields.get(player2.getId()).add(bear);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.MENACE)).isFalse();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.HASTE)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "Target creature you control gets +2/+0 until end of turn",
            "Target creature you control gains menace and haste until end of turn"
    })
    @DisplayName("The trigger resolves independently of Ferocification")
    void removingSourceDoesNotStopTrigger(String mode) {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new Ferocification());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToBeginningOfCombat(player1);
        harness.handleListChoice(player1, mode);
        harness.handlePermanentChosen(player1, bear.getId());
        gd.playerBattlefields.get(player1.getId()).remove(source);
        harness.passBothPriorities();

        boolean boostMode = mode.contains("+2/+0");
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(boostMode ? 4 : 2);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.MENACE)).isEqualTo(!boostMode);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.HASTE)).isEqualTo(!boostMode);
    }

    private void advanceToBeginningOfCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
    }
}
