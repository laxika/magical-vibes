package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AugmentingAutomaton;
import com.github.laxika.magicalvibes.cards.c.CountlessGearsRenegade;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FourthBridgeProwler.class, CountlessGearsRenegade.class, AugmentingAutomaton.class})
class FourthBridgeProwlerTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the ETB trigger gives target creature -1/-1")
    void acceptingTriggerDebuffsTarget() {
        harness.addToBattlefield(player2, new CountlessGearsRenegade());
        UUID targetId = harness.getPermanentId(player2, "Countless Gears Renegade");

        castProwler();
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent target = findPermanent(player2, "Countless Gears Renegade");
        assertThat(target.getEffectivePower()).isEqualTo(1);
        assertThat(target.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining the ETB trigger leaves the target unchanged")
    void decliningTriggerLeavesTargetUnchanged() {
        harness.addToBattlefield(player2, new CountlessGearsRenegade());
        UUID targetId = harness.getPermanentId(player2, "Countless Gears Renegade");

        castProwler();
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        Permanent target = findPermanent(player2, "Countless Gears Renegade");
        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("The ETB trigger kills a 1/1 creature")
    void debuffKillsOneOneCreature() {
        harness.addToBattlefield(player2, new AugmentingAutomaton());
        UUID targetId = harness.getPermanentId(player2, "Augmenting Automaton");

        castProwler();
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player2, "Augmenting Automaton");
        harness.assertInGraveyard(player2, "Augmenting Automaton");
    }

    @Test
    @DisplayName("The debuff wears off at end of turn")
    void debuffWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player2, new CountlessGearsRenegade());
        UUID targetId = harness.getPermanentId(player2, "Countless Gears Renegade");

        castProwler();
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent target = findPermanent(player2, "Countless Gears Renegade");
        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("On an otherwise empty battlefield, Prowler targets itself and may decline")
    void canDeclineWithOnlyItselfAsTarget() {
        castProwler();
        UUID prowlerId = harness.getPermanentId(player1, "Fourth Bridge Prowler");
        harness.handlePermanentChosen(player1, prowlerId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Fourth Bridge Prowler");
        harness.assertNotInGraveyard(player1, "Fourth Bridge Prowler");
    }

    @Test
    @DisplayName("Prowler can accept its trigger targeting itself and die")
    void canKillItselfWithItsTrigger() {
        castProwler();
        UUID prowlerId = harness.getPermanentId(player1, "Fourth Bridge Prowler");
        harness.handlePermanentChosen(player1, prowlerId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Fourth Bridge Prowler");
        harness.assertInGraveyard(player1, "Fourth Bridge Prowler");
    }

    @Test
    @DisplayName("Prowler can target another creature its controller controls")
    void canDebuffAnotherFriendlyCreature() {
        harness.addToBattlefield(player1, new CountlessGearsRenegade());
        UUID targetId = harness.getPermanentId(player1, "Countless Gears Renegade");

        castProwler();
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent target = findPermanent(player1, "Countless Gears Renegade");
        assertThat(target.getEffectivePower()).isEqualTo(1);
        assertThat(target.getEffectiveToughness()).isEqualTo(1);
    }

    private void castProwler() {
        harness.castFromHand(player1, new FourthBridgeProwler(), "{B}");
        harness.passBothPriorities();
    }
}