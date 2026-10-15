package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BlindPhantasm;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TaroxBladewing.class, BlindPhantasm.class})
class TaroxBladewingTest extends BaseCardTest {

    @Test
    @DisplayName("Grandeur discards another Tarox and adds its current power to both stats")
    void grandeurBoostsByCurrentPower() {
        Permanent tarox = harness.addToBattlefieldAndReturn(player1, new TaroxBladewing());
        harness.setHand(player1, List.of(new TaroxBladewing()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, tarox)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, tarox)).isEqualTo(7);
        harness.assertInGraveyard(player1, "Tarox Bladewing");
    }

    @Test
    @DisplayName("Each grandeur activation uses the power after previous boosts")
    void repeatedGrandeurActivationsCompound() {
        Permanent tarox = harness.addToBattlefieldAndReturn(player1, new TaroxBladewing());
        harness.setHand(player1, List.of(new TaroxBladewing(), new TaroxBladewing()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, tarox)).isEqualTo(16);
        assertThat(gqs.getEffectiveToughness(gd, tarox)).isEqualTo(15);
    }

    @Test
    @DisplayName("Stacked grandeur abilities each use Tarox's power when they resolve")
    void stackedGrandeurActivationsUsePowerAtResolution() {
        Permanent tarox = harness.addToBattlefieldAndReturn(player1, new TaroxBladewing());
        harness.setHand(player1, List.of(new TaroxBladewing(), new TaroxBladewing()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gqs.getEffectivePower(gd, tarox)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, tarox)).isEqualTo(3);

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, tarox)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, tarox)).isEqualTo(7);

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, tarox)).isEqualTo(16);
        assertThat(gqs.getEffectiveToughness(gd, tarox)).isEqualTo(15);
    }

    @Test
    @DisplayName("The grandeur boost wears off at end of turn")
    void grandeurBoostWearsOffAtEndOfTurn() {
        Permanent tarox = harness.addToBattlefieldAndReturn(player1, new TaroxBladewing());
        harness.setHand(player1, List.of(new TaroxBladewing()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, tarox)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, tarox)).isEqualTo(3);
    }

    @Test
    @DisplayName("Grandeur requires another card named Tarox Bladewing")
    void grandeurRequiresAnotherTarox() {
        harness.addToBattlefieldAndReturn(player1, new TaroxBladewing());
        harness.setHand(player1, List.of(new BlindPhantasm()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Grandeur discards only the named card from a mixed hand")
    void grandeurDiscardsNamedCardFromMixedHand() {
        Permanent tarox = harness.addToBattlefieldAndReturn(player1, new TaroxBladewing());
        harness.setHand(player1, List.of(new BlindPhantasm(), new TaroxBladewing()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .singleElement()
                .isInstanceOf(BlindPhantasm.class);
        assertThat(gqs.getEffectivePower(gd, tarox)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, tarox)).isEqualTo(7);
        harness.assertInGraveyard(player1, "Tarox Bladewing");
    }
}
