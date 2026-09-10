package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(ThranWarMachine.class)
class ThranWarMachineTest extends BaseCardTest {

    @Test
    @DisplayName("Thran War Machine must attack each combat when able")
    void mustAttackWhenAble() {
        addCreatureReady(player1, new ThranWarMachine());

        assertThatThrownBy(() -> declareAttackers(List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Thran War Machine can attack when able")
    void canAttackWhenAble() {
        addCreatureReady(player1, new ThranWarMachine());

        declareAttackers(List.of(0));

        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
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
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();

        advanceToUpkeep(player1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Thran War Machine");
    }

    @Test
    @DisplayName("Echo waits for Thran War Machine's controller's upkeep")
    void echoDoesNotTriggerDuringOpponentsUpkeep() {
        castAndResolveThranWarMachine();

        advanceToUpkeep(player2);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Thran War Machine");

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    private void castAndResolveThranWarMachine() {
        harness.castFromHand(player1, new ThranWarMachine(), "{4}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Thran War Machine");
    }
}
