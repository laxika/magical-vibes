package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CuombajjWitchesTest extends BaseCardTest {

    @Test
    @DisplayName("Controller chooses the first target and opponent chooses the second")
    void controllerChoosesFirstTargetAndOpponentChoosesSecond() {
        addWitchesReady();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player1.getId());

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validPlayerIds()).contains(player1.getId(), player2.getId());

        harness.handlePermanentChosen(player2, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Opponent may choose a creature as the second target")
    void opponentChoosesCreatureAsSecondTarget() {
        addWitchesReady();
        harness.addToBattlefield(player2, new LlanowarElves());
        Permanent elf = findPermanent(player2, "Llanowar Elves");

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.handlePermanentChosen(player2, elf.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Llanowar Elves");
        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Cannot choose a non-creature permanent as an any target")
    void cannotChooseNonCreaturePermanent() {
        addWitchesReady();
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid first target");
    }

    private void addWitchesReady() {
        addCreatureReady(player1, new CuombajjWitches());
    }
}
