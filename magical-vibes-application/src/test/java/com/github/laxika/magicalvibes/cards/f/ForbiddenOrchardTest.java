package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ForbiddenOrchardTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping for mana adds the chosen color and gives the opponent a Spirit token")
    void tapForManaGivesOpponentSpirit() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new ForbiddenOrchard());
        int before = gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE);

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(before + 1);
        assertThat(land.isTapped()).isTrue();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Spirit"))
                .singleElement()
                .satisfies(spirit -> {
                    assertThat(spirit.getCard().getPower()).isEqualTo(1);
                    assertThat(spirit.getCard().getToughness()).isEqualTo(1);
                });
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Spirit"));
    }

    @Test
    @DisplayName("A second activation on a later turn makes a second Spirit for the opponent")
    void eachTapMakesAnotherSpirit() {
        harness.addToBattlefieldAndReturn(player1, new ForbiddenOrchard());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        advanceTurn();
        advanceTurn();

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Spirit"))
                .hasSize(2);
    }

    @Test
    @DisplayName("No Spirit is created while the land is untapped")
    void noSpiritWithoutTapping() {
        harness.addToBattlefield(player1, new ForbiddenOrchard());

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Spirit"));
    }

    private void advanceTurn() {
        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
