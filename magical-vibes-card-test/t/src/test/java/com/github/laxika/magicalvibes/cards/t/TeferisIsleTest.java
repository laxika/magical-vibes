package com.github.laxika.magicalvibes.cards.t;

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

@CardUsed(TeferisIsle.class)
class TeferisIsleTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new TeferisIsle()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Teferi's Isle").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tap ability adds two blue mana")
    void tapAddsTwoBlueMana() {
        Permanent isle = harness.addToBattlefieldAndReturn(player1, new TeferisIsle());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(2);
        assertThat(isle.isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "Teferi's Isle");
    }

    @Test
    @DisplayName("Phases out before untapping and phases in during the next untap step")
    void phasesOutBeforeUntappingAndPhasesInDuringNextUntapStep() {
        harness.setHand(player1, List.of(new TeferisIsle()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player1, 0);

        Permanent isle = findPermanent(player1, "Teferi's Isle");
        assertThat(isle.isTapped()).isTrue();

        harness.performUntapStep(player1);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(isle);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).containsExactly(isle);

        harness.performUntapStep(player2);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).containsExactly(isle);

        harness.performUntapStep(player1);

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(isle);
        assertThat(gd.phasedOutPermanents.getOrDefault(player1.getId(), List.of())).doesNotContain(isle);
        assertThat(isle.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate the mana ability while tapped")
    void cannotActivateWhileTapped() {
        Permanent isle = harness.enterBattlefieldAndReturn(player1, new TeferisIsle());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(isle.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Phasing repeats and the returned Isle produces mana without using the stack")
    void repeatedPhasingPreservesManaAbility() {
        Permanent isle = harness.enterBattlefieldAndReturn(player1, new TeferisIsle());

        for (int cycle = 0; cycle < 2; cycle++) {
            harness.performUntapStep(player1);

            harness.assertNotOnBattlefield(player1, "Teferi's Isle");
            assertThat(gd.phasedOutPermanents.get(player1.getId())).containsExactly(isle);
            assertThat(isle.isTapped()).isTrue();

            harness.performUntapStep(player2);
            harness.assertNotOnBattlefield(player1, "Teferi's Isle");

            harness.performUntapStep(player1);

            assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(isle);
            assertThat(isle.isTapped()).isFalse();
            int blueBefore = gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE);

            harness.activateAbility(player1, 0, 0, null, null);

            assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE))
                    .isEqualTo(blueBefore + 2);
            assertThat(isle.isTapped()).isTrue();
            assertThat(gd.stack).isEmpty();
        }
    }
}
