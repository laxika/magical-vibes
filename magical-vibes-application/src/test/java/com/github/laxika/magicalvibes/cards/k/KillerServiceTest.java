package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(KillerService.class)
class KillerServiceTest extends BaseCardTest {

    @Test
    @DisplayName("Creates one Food token for one opponent")
    void createsFoodForEachOpponent() {
        harness.enterBattlefieldAndReturn(player1, new KillerService());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Food")).isEqualTo(1);
    }

    @Test
    @DisplayName("Pays and sacrifices a token to create a Rhino Warrior")
    void paysAndSacrificesTokenToCreateRhinoWarrior() {
        harness.enterBattlefieldAndReturn(player1, new KillerService());
        harness.passBothPriorities();
        Permanent food = findPermanent(player1, "Food");
        advanceToEndStep();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, food.getId());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Food")).isZero();
        Permanent rhino = findPermanent(player1, "Rhino Warrior");
        assertThat(rhino.getCard().getPower()).isEqualTo(4);
        assertThat(rhino.getCard().getToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Declining the end-step ability keeps the token")
    void mayBeDeclined() {
        harness.enterBattlefieldAndReturn(player1, new KillerService());
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        advanceToEndStep();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(countPermanents(player1, "Food")).isOne();
        assertThat(countPermanents(player1, "Rhino Warrior")).isZero();
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
