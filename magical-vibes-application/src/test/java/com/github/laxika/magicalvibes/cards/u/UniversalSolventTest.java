package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UniversalSolvent.class, Ornithopter.class})
class UniversalSolventTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices itself and destroys target permanent")
    void sacrificesItselfAndDestroysTargetPermanent() {
        harness.addToBattlefield(player1, new UniversalSolvent());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.assertNotOnBattlefield(player1, "Universal Solvent");
        harness.assertInGraveyard(player1, "Universal Solvent");

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Ornithopter");
        harness.assertInGraveyard(player2, "Ornithopter");
    }

    @Test
    @DisplayName("Cannot activate without seven mana")
    void cannotActivateWithoutSevenMana() {
        harness.addToBattlefield(player1, new UniversalSolvent());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate a tapped Solvent")
    void cannotActivateWhileTapped() {
        Permanent solvent = harness.addToBattlefieldAndReturn(player1, new UniversalSolvent());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        solvent.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Universal Solvent");
        harness.assertNotInGraveyard(player1, "Universal Solvent");
        harness.assertOnBattlefield(player2, "Ornithopter");
    }

    @Test
    @DisplayName("Can destroy a noncreature permanent")
    void destroysNoncreaturePermanent() {
        harness.addToBattlefield(player1, new UniversalSolvent());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new UniversalSolvent());
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.assertOnBattlefield(player2, "Universal Solvent");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Universal Solvent");
        harness.assertInGraveyard(player2, "Universal Solvent");
    }

    @Test
    @DisplayName("Can destroy a permanent its controller controls")
    void destroysFriendlyPermanent() {
        harness.addToBattlefield(player1, new UniversalSolvent());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Ornithopter");
        harness.assertInGraveyard(player1, "Ornithopter");
    }

    @Test
    @DisplayName("Can target itself even though it is sacrificed as a cost")
    void canTargetItself() {
        Permanent solvent = harness.addToBattlefieldAndReturn(player1, new UniversalSolvent());
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.activateAbility(player1, 0, null, solvent.getId());
        harness.assertNotOnBattlefield(player1, "Universal Solvent");
        harness.assertInGraveyard(player1, "Universal Solvent");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Universal Solvent");
    }

    @Test
    @DisplayName("An absent target does not refund the sacrifice cost")
    void targetCanLeaveBeforeResolution() {
        harness.addToBattlefield(player1, new UniversalSolvent());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new UniversalSolvent());
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.addMana(player2, ManaColor.COLORLESS, 7);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.activateAbility(player2, 0, null, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Universal Solvent");
        harness.assertInGraveyard(player1, "Universal Solvent");
        harness.assertNotOnBattlefield(player2, "Universal Solvent");
        harness.assertInGraveyard(player2, "Universal Solvent");
    }
}
