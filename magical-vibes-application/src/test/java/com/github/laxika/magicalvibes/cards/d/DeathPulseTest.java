package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GustcloakSentinel;
import com.github.laxika.magicalvibes.cards.k.KrosanColossus;
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

@CardUsed({DeathPulse.class, GustcloakSentinel.class, KrosanColossus.class})
class DeathPulseTest extends BaseCardTest {

    @Test
    @DisplayName("Gives target creature -4/-4")
    void givesTargetCreatureMinusFourMinusFour() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GustcloakSentinel());
        harness.setHand(player1, List.of(new DeathPulse()));
        addSpellMana();

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Gustcloak Sentinel");
        harness.assertInGraveyard(player2, "Gustcloak Sentinel");
    }

    @Test
    @DisplayName("Gives a surviving target creature exactly -4/-4")
    void givesTargetCreatureExactlyMinusFourMinusFour() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KrosanColossus());
        harness.setHand(player1, List.of(new DeathPulse()));
        addSpellMana();

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(-4);
        assertThat(target.getToughnessModifier()).isEqualTo(-4);
        assertThat(target.getEffectivePower()).isEqualTo(5);
        assertThat(target.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("Cycling gives a target creature -1/-1 and draws a card")
    void cyclingDebuffsCreatureAndDraws() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GustcloakSentinel());
        harness.setHand(player1, List.of(new DeathPulse()));
        harness.setLibrary(player1, List.of(new GustcloakSentinel()));
        addCyclingMana();

        cycleTarget(target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(-1);
        assertThat(target.getToughnessModifier()).isEqualTo(-1);
        harness.assertInGraveyard(player1, "Death Pulse");
        harness.assertInHand(player1, "Gustcloak Sentinel");
    }

    @Test
    @DisplayName("Cycling may be declined and still draws a card")
    void cyclingMayBeDeclined() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GustcloakSentinel());
        harness.setHand(player1, List.of(new DeathPulse()));
        harness.setLibrary(player1, List.of(new GustcloakSentinel()));
        addCyclingMana();

        cycleTarget(target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Gustcloak Sentinel");
        harness.assertInHand(player1, "Gustcloak Sentinel");
    }

    @Test
    @DisplayName("Cycling with no creature target still draws a card")
    void cyclingWithNoCreatureTargetStillDraws() {
        harness.setHand(player1, List.of(new DeathPulse()));
        harness.setLibrary(player1, List.of(new GustcloakSentinel()));
        addCyclingMana();

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Death Pulse");
        harness.assertInHand(player1, "Gustcloak Sentinel");
    }

    @Test
    @DisplayName("Cycling debuff wears off at end of turn")
    void cyclingDebuffWearsOffAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GustcloakSentinel());
        harness.setHand(player1, List.of(new DeathPulse()));
        harness.setLibrary(player1, List.of(new GustcloakSentinel()));
        addCyclingMana();

        cycleTarget(target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        assertThat(target.getPowerModifier()).isEqualTo(-1);
        assertThat(target.getToughnessModifier()).isEqualTo(-1);

        harness.passUntil(TurnStep.CLEANUP);

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The main spell cannot target a player")
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new DeathPulse()));
        addSpellMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The cycling trigger resolves before the separate draw ability")
    void cyclingTriggerResolvesBeforeDraw() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GustcloakSentinel());
        harness.setHand(player1, List.of(new DeathPulse()));
        harness.setLibrary(player1, List.of(new KrosanColossus()));
        addCyclingMana();

        cycleTarget(target.getId());
        harness.assertInGraveyard(player1, "Death Pulse");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
        harness.assertNotInHand(player1, "Krosan Colossus");

        harness.passBothPriorities();
        harness.assertInHand(player1, "Krosan Colossus");
    }

    @Test
    @DisplayName("Losing the cycling trigger's target does not prevent drawing")
    void cyclingDrawsWhenTriggerTargetLeavesBattlefield() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GustcloakSentinel());
        harness.setHand(player1, List.of(new DeathPulse(), new DeathPulse()));
        harness.setLibrary(player1, List.of(new KrosanColossus()));
        addCyclingMana();
        addSpellMana();

        cycleTarget(target.getId());
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Gustcloak Sentinel");

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Krosan Colossus");
    }

    @Test
    @DisplayName("The main spell's debuff wears off at cleanup")
    void spellDebuffWearsOffAtCleanup() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KrosanColossus());
        harness.setHand(player1, List.of(new DeathPulse()));
        addSpellMana();

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        assertThat(target.getEffectiveToughness()).isEqualTo(5);

        harness.passUntil(TurnStep.CLEANUP);

        assertThat(target.getEffectivePower()).isEqualTo(9);
        assertThat(target.getEffectiveToughness()).isEqualTo(9);
    }

    private void cycleTarget(java.util.UUID targetId) {
        harness.activateHandAbility(player1, 0, null);
        harness.handlePermanentChosen(player1, targetId);
    }

    private void addSpellMana() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private void addCyclingMana() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
