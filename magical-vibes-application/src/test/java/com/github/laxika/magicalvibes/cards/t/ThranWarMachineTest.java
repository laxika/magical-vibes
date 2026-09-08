package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ThranWarMachineTest extends BaseCardTest {

    @Test
    @DisplayName("Thran War Machine must attack each combat when able")
    void mustAttackWhenAble() {
        Permanent machine = new Permanent(new ThranWarMachine());
        machine.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(machine);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Declining echo sacrifices Thran War Machine at its next upkeep")
    void decliningEchoSacrificesThranWarMachine() {
        castAndResolveThranWarMachine();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Thran War Machine");
        harness.assertInGraveyard(player1, "Thran War Machine");
    }

    @Test
    @DisplayName("Paying echo keeps Thran War Machine and echo does not trigger again")
    void payingEchoKeepsThranWarMachineAndIsOneShot() {
        castAndResolveThranWarMachine();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Thran War Machine");

        advanceToUpkeep(player1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Thran War Machine");
    }

    private void castAndResolveThranWarMachine() {
        harness.setHand(player1, List.of(new ThranWarMachine()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Thran War Machine");
    }
}
