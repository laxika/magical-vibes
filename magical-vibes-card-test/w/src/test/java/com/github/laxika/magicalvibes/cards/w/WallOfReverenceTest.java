package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WallOfReverence.class, GrizzlyBears.class, HillGiant.class, Unsummon.class})
class WallOfReverenceTest extends BaseCardTest {

    /**
     * Advance to the controller's end step. The trigger targets as it is put onto the stack
     * (CR 603.3d), so the target prompt comes first; the "may" prompt follows at resolution.
     */
    private void advanceToEndStepTargetPrompt() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
    }

    @Test
    @DisplayName("Accepting gains life equal to the chosen creature's power")
    void acceptGainsLifeEqualToTargetPower() {
        harness.addToBattlefield(player1, new WallOfReverence());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLife(player1, 20);

        advanceToEndStepTargetPrompt();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities(); // resolve the MayEffect → may prompt
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Life gained equals the specific chosen creature's power, not another's")
    void gainsChosenCreaturePower() {
        harness.addToBattlefield(player1, new WallOfReverence());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.setLife(player1, 20);

        advanceToEndStepTargetPrompt();
        harness.handlePermanentChosen(player1, giant.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("Declining gains no life")
    void declineGainsNoLife() {
        harness.addToBattlefield(player1, new WallOfReverence());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLife(player1, 20);

        advanceToEndStepTargetPrompt();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("An opponent's creature is not a legal target")
    void opponentCreatureNotTargetable() {
        harness.addToBattlefield(player1, new WallOfReverence());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLife(player1, 20);

        advanceToEndStepTargetPrompt();

        // Only creatures player1 controls are offered — the opponent's creature is excluded.
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).doesNotContain(opponentBears.getId());
    }

    @Test
    @DisplayName("The Wall can target itself and gains life using power rather than toughness")
    void canTargetItself() {
        Permanent wall = harness.addToBattlefieldAndReturn(player1, new WallOfReverence());
        harness.setLife(player1, 20);

        advanceToEndStepTargetPrompt();
        harness.handlePermanentChosen(player1, wall.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The ability does not trigger during an opponent's end step")
    void doesNotTriggerOnOpponentEndStep() {
        harness.addToBattlefield(player1, new WallOfReverence());
        harness.setLife(player1, 20);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.passUntil(player2, TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Power is evaluated at resolution rather than when the target is chosen")
    void usesPowerAtResolution() {
        Permanent wall = harness.addToBattlefieldAndReturn(player1, new WallOfReverence());
        harness.setLife(player1, 20);

        advanceToEndStepTargetPrompt();
        harness.handlePermanentChosen(player1, wall.getId());
        wall.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 24);
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, -3})
    @DisplayName("Zero or negative target power gains no life and does not cause life loss")
    void nonpositivePowerGainsNoLife(int powerModifier) {
        Permanent wall = harness.addToBattlefieldAndReturn(player1, new WallOfReverence());
        wall.setPowerModifier(powerModifier);
        harness.setLife(player1, 20);

        advanceToEndStepTargetPrompt();
        harness.handlePermanentChosen(player1, wall.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("A target returned to hand causes the ability to fail without a may prompt")
    void removedTargetDoesNotGainLife() {
        Permanent wall = harness.addToBattlefieldAndReturn(player1, new WallOfReverence());
        harness.setLife(player1, 20);

        advanceToEndStepTargetPrompt();
        harness.handlePermanentChosen(player1, wall.getId());
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, wall.getId());
        harness.assertInHand(player1, "Wall of Reverence");
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The ability still gains life if the Wall leaves while its target remains legal")
    void sourceLeavingDoesNotStopLifeGain() {
        Permanent wall = harness.addToBattlefieldAndReturn(player1, new WallOfReverence());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLife(player1, 20);

        advanceToEndStepTargetPrompt();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, wall.getId());
        harness.assertInHand(player1, "Wall of Reverence");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("A target that changes controllers is no longer legal at resolution")
    void targetChangingControllersDoesNotGainLife() {
        Permanent wall = harness.addToBattlefieldAndReturn(player1, new WallOfReverence());
        harness.setLife(player1, 20);

        advanceToEndStepTargetPrompt();
        harness.handlePermanentChosen(player1, wall.getId());
        gd.playerBattlefields.get(player1.getId()).remove(wall);
        gd.playerBattlefields.get(player2.getId()).add(wall);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("A Wall entering after the end step begins does not trigger that turn")
    void enteringDuringEndStepDoesNotTrigger() {
        harness.setLife(player1, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);

        harness.enterBattlefieldAndReturn(player1, new WallOfReverence());

        harness.assertOnBattlefield(player1, "Wall of Reverence");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertLife(player1, 20);
    }
}
