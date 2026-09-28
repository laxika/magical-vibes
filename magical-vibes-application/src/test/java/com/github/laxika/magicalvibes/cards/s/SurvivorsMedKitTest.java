package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SurvivorsMedKit.class})
class SurvivorsMedKitTest extends BaseCardTest {

    @Test
    void stimpakDrawsACardAndCannotBeChosenAgain() {
        Permanent medKit = harness.addToBattlefieldAndReturn(player1, new SurvivorsMedKit());
        harness.setLibrary(player1, List.of(new SurvivorsMedKit()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 0, 0, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        medKit.untap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("That mode has already been chosen");
    }

    @Test
    void fancyLadsSnackCakesCreatesFood() {
        harness.addToBattlefieldAndReturn(player1, new SurvivorsMedKit());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, 1, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Food")).hasSize(1);
    }

    @Test
    void radAwayRemovesAllRadCountersAndSacrificesTheMedKit() {
        harness.addToBattlefieldAndReturn(player1, new SurvivorsMedKit());
        gd.playerRadCounters.put(player2.getId(), 4);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, 2, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerRadCounters.getOrDefault(player2.getId(), 0)).isZero();
        harness.assertInGraveyard(player1, "Survivor's Med Kit");
    }
}
