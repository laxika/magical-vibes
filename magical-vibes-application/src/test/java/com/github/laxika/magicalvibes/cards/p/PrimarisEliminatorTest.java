package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
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

@CardUsed({PrimarisEliminator.class, GrizzlyBears.class, HillGiant.class})
class PrimarisEliminatorTest extends BaseCardTest {

    @Test
    @DisplayName("Executioner Round destroys target creature")
    void executionerRoundDestroysTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castPrimarisEliminator(0, target.getId());

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Primaris Eliminator");
    }

    @Test
    @DisplayName("Hyperfrag Round weakens creatures controlled by the target player")
    void hyperfragRoundWeakensTargetPlayersCreaturesUntilEndOfTurn() {
        Permanent targetCreature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castPrimarisEliminator(1, player2.getId());

        assertThat(targetCreature.getEffectivePower()).isEqualTo(1);
        assertThat(targetCreature.getEffectiveToughness()).isEqualTo(1);
        assertThat(ownCreature.getEffectivePower()).isEqualTo(2);
        assertThat(ownCreature.getEffectiveToughness()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(targetCreature.getEffectivePower()).isEqualTo(3);
        assertThat(targetCreature.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Each mode only accepts its own target type")
    void eachModeOnlyAcceptsItsOwnTargetType() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new PrimarisEliminator()));
        addPrimarisEliminatorMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Creatures target player controls get -2/-2 until end of turn");

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Hyperfrag Round can target its controller and kills the Eliminator too")
    void hyperfragRoundCanTargetItsController() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        castPrimarisEliminator(1, player1.getId());

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Primaris Eliminator");
        harness.assertInGraveyard(player1, "Primaris Eliminator");
        assertThat(opponent.getEffectivePower()).isEqualTo(3);
        assertThat(opponent.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Hyperfrag Round kills small creatures but does not affect later entrants")
    void hyperfragRoundDoesNotAffectCreaturesEnteringAfterResolution() {
        harness.addToBattlefield(player2, new GrizzlyBears());

        castPrimarisEliminator(1, player2.getId());

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        Permanent laterCreature = harness.enterBattlefieldAndReturn(player2, new HillGiant());
        assertThat(laterCreature.getEffectivePower()).isEqualTo(3);
        assertThat(laterCreature.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Entering without being cast still allows choosing Hyperfrag Round")
    void enteringWithoutBeingCastAllowsEitherMode() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        harness.enterBattlefieldAndReturn(player1, new PrimarisEliminator());
        chooseModeAndTarget(1, player2.getId());
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(1);
        assertThat(target.getEffectiveToughness()).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Primaris Eliminator");
    }

    @Test
    @DisplayName("Executioner Round can target the Eliminator itself after it enters")
    void executionerRoundCanDestroyItsSource() {
        harness.setHand(player1, List.of(new PrimarisEliminator()));
        addPrimarisEliminatorMana();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        chooseModeAndTarget(0, findPermanent(player1, "Primaris Eliminator").getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Primaris Eliminator");
        harness.assertInGraveyard(player1, "Primaris Eliminator");
    }

    private void chooseModeAndTarget(int mode, java.util.UUID targetId) {
        harness.handleListChoice(player1, mode == 0
                ? "Destroy target creature"
                : "Creatures target player controls get -2/-2 until end of turn");
        harness.handlePermanentChosen(player1, targetId);
    }

    private void castPrimarisEliminator(int mode, java.util.UUID targetId) {
        harness.setHand(player1, List.of(new PrimarisEliminator()));
        addPrimarisEliminatorMana();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        chooseModeAndTarget(mode, targetId);
        harness.passBothPriorities();
    }

    private void addPrimarisEliminatorMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
