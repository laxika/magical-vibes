package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(IllusionsOfGrandeur.class)
class IllusionsOfGrandeurTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield gains 20 life")
    void entryGainsTwentyLife() {
        harness.setHand(player1, List.of(new IllusionsOfGrandeur()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities(); // enchantment resolves, ETB trigger goes on stack
        harness.passBothPriorities(); // ETB trigger resolves

        harness.assertLife(player1, 40);
    }

    @Test
    @DisplayName("Leaving the battlefield loses 20 life")
    void leavingLosesTwentyLife() {
        harness.addToBattlefield(player1, new IllusionsOfGrandeur());
        harness.setLife(player1, 40);

        Permanent illusions = findPermanent(player1, "Illusions of Grandeur");

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, illusions));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // LTB trigger resolves

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Paying cumulative upkeep keeps Illusions of Grandeur")
    void paysCumulativeUpkeep() {
        Permanent illusions = harness.addToBattlefieldAndReturn(player1, new IllusionsOfGrandeur());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(illusions.getCounterCount(CounterType.AGE)).isEqualTo(1);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(illusions);
    }

    @Test
    @DisplayName("Cumulative upkeep cost increases with age counters")
    void cumulativeUpkeepCostIncreases() {
        Permanent illusions = harness.addToBattlefieldAndReturn(player1, new IllusionsOfGrandeur());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player1, true);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(illusions.getCounterCount(CounterType.AGE)).isEqualTo(2);

        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(illusions);
    }

    @Test
    @DisplayName("Declining cumulative upkeep sacrifices Illusions of Grandeur")
    void declineSacrifices() {
        Permanent illusions = harness.addToBattlefieldAndReturn(player1, new IllusionsOfGrandeur());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(illusions);
        harness.assertInGraveyard(player1, "Illusions of Grandeur");
    }

    @Test
    @DisplayName("Declining cumulative upkeep triggers the leave-the-battlefield life loss")
    void declineTriggersLeaveLifeLoss() {
        harness.setLife(player1, 40);
        harness.addToBattlefield(player1, new IllusionsOfGrandeur());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }
}
