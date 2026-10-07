package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SteelgazeGriffin.class})
class SteelgazeGriffinTest extends BaseCardTest {

    @Test
    @DisplayName("Drawing the second card each turn gives Steelgaze Griffin +2/+0 only once")
    void boostsOnSecondDrawOnlyOnce() {
        Permanent griffin = harness.addToBattlefieldAndReturn(player1, new SteelgazeGriffin());
        harness.setLibrary(player1, List.of(new SteelgazeGriffin(), new SteelgazeGriffin(), new SteelgazeGriffin()));

        drawCard();
        assertThat(griffin.getPowerModifier()).isZero();

        drawCard();
        assertThat(gd.stack).hasSize(1);
        drawCard();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(griffin.getPowerModifier()).isEqualTo(2);
        assertThat(griffin.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("An opponent's second draw does not boost Steelgaze Griffin")
    void ignoresOpponentDraws() {
        Permanent griffin = harness.addToBattlefieldAndReturn(player1, new SteelgazeGriffin());
        harness.setLibrary(player2, List.of(new SteelgazeGriffin(), new SteelgazeGriffin()));

        harness.inMutationScope(() -> {
            harness.getDrawService().resolveDrawCard(gd, player2.getId());
            harness.getDrawService().resolveDrawCard(gd, player2.getId());
        });

        assertThat(gd.stack).isEmpty();
        assertThat(griffin.getPowerModifier()).isZero();
        assertThat(griffin.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The first draw counts even when it happened before Steelgaze Griffin entered")
    void countsDrawBeforeEntering() {
        harness.setLibrary(player1, List.of(new SteelgazeGriffin(), new SteelgazeGriffin()));
        drawCard();
        Permanent griffin = harness.addToBattlefieldAndReturn(player1, new SteelgazeGriffin());

        drawCard();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(griffin.getPowerModifier()).isEqualTo(2);
        assertThat(griffin.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Entering after the second draw does not trigger on a later draw")
    void doesNotTriggerAfterSecondDrawAlreadyHappened() {
        harness.setLibrary(player1, List.of(new SteelgazeGriffin(), new SteelgazeGriffin(),
                new SteelgazeGriffin()));
        drawCard();
        drawCard();
        Permanent griffin = harness.addToBattlefieldAndReturn(player1, new SteelgazeGriffin());

        drawCard();

        assertThat(gd.stack).isEmpty();
        assertThat(griffin.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("The boost expires and the second draw can trigger again on an opponent's turn")
    void expiresAndTriggersAgainNextTurn() {
        Permanent griffin = harness.addToBattlefieldAndReturn(player1, new SteelgazeGriffin());
        harness.setLibrary(player1, List.of(new SteelgazeGriffin(), new SteelgazeGriffin(),
                new SteelgazeGriffin(), new SteelgazeGriffin()));
        harness.setLibrary(player2, List.of(new SteelgazeGriffin(), new SteelgazeGriffin()));
        drawCard();
        drawCard();
        harness.passBothPriorities();
        assertThat(griffin.getPowerModifier()).isEqualTo(2);

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(griffin.getPowerModifier()).isZero();
        assertThat(griffin.getToughnessModifier()).isZero();

        drawCard();
        assertThat(gd.stack).isEmpty();
        drawCard();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(griffin.getPowerModifier()).isEqualTo(2);
        assertThat(griffin.getToughnessModifier()).isZero();
    }

    private void drawCard() {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
    }
}
