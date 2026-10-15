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
        harness.addToBattlefield(player1, new SurvivorsMedKit());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, 1, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Food")).hasSize(1);
    }

    @Test
    void radAwayRemovesAllRadCountersAndSacrificesTheMedKit() {
        harness.addToBattlefield(player1, new SurvivorsMedKit());
        gd.playerRadCounters.put(player2.getId(), 4);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, 2, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerRadCounters.getOrDefault(player2.getId(), 0)).isZero();
        harness.assertInGraveyard(player1, "Survivor's Med Kit");
    }

    @Test
    void modeIsUsedAsSoonAsTheAbilityIsActivated() {
        Permanent medKit = harness.addToBattlefieldAndReturn(player1, new SurvivorsMedKit());
        harness.setLibrary(player1, List.of(new SurvivorsMedKit()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, 0, null);
        medKit.untap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("That mode has already been chosen");

        harness.passBothPriorities();
        harness.assertInHand(player1, "Survivor's Med Kit");
    }

    @Test
    void differentMedKitsCanChooseTheSameMode() {
        harness.addToBattlefield(player1, new SurvivorsMedKit());
        harness.addToBattlefield(player1, new SurvivorsMedKit());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, 1, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, 0, 1, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Food")).hasSize(2);
    }

    @Test
    void foodCanBeSacrificedForThreeLife() {
        harness.addToBattlefield(player1, new SurvivorsMedKit());
        harness.setLife(player1, 10);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, 1, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, null, null);

        assertThat(findPermanents(player1, "Food")).isEmpty();
        harness.assertLife(player1, 10);
        harness.passBothPriorities();
        harness.assertLife(player1, 13);
    }

    @Test
    void radAwayCanTargetItsControllerAndLeavesOtherPlayersCountersAlone() {
        harness.addToBattlefield(player1, new SurvivorsMedKit());
        gd.playerRadCounters.put(player1.getId(), 3);
        gd.playerRadCounters.put(player2.getId(), 5);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, 2, player1.getId());

        harness.assertOnBattlefield(player1, "Survivor's Med Kit");
        harness.assertNotInGraveyard(player1, "Survivor's Med Kit");
        assertThat(gd.playerRadCounters.get(player1.getId())).isEqualTo(3);
        harness.passBothPriorities();

        assertThat(gd.playerRadCounters.getOrDefault(player1.getId(), 0)).isZero();
        assertThat(gd.playerRadCounters.get(player2.getId())).isEqualTo(5);
        harness.assertNotOnBattlefield(player1, "Survivor's Med Kit");
        harness.assertInGraveyard(player1, "Survivor's Med Kit");
    }

    @Test
    void radAwayStillSacrificesTheMedKitWhenTargetHasNoRadCounters() {
        harness.addToBattlefield(player1, new SurvivorsMedKit());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, 2, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerRadCounters.getOrDefault(player2.getId(), 0)).isZero();
        harness.assertNotOnBattlefield(player1, "Survivor's Med Kit");
        harness.assertInGraveyard(player1, "Survivor's Med Kit");
    }
}
