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

@CardUsed({KillerService.class})
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

        advanceToEndStep();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(countPermanents(player1, "Food")).isOne();
        assertThat(countPermanents(player1, "Rhino Warrior")).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }

    @Test
    @DisplayName("Creates the Rhino during the original end-step ability's resolution")
    void createsRhinoWithoutAnotherPriorityRound() {
        harness.enterBattlefieldAndReturn(player1, new KillerService());
        harness.passBothPriorities();
        Permanent food = findPermanent(player1, "Food");
        advanceToEndStep();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, food.getId());

        assertThat(countPermanents(player1, "Food")).isZero();
        assertThat(countPermanents(player1, "Rhino Warrior")).isOne();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Food can be sacrificed immediately for three life")
    void foodAbilityPaysManaAndSacrificesAsCost() {
        harness.enterBattlefieldAndReturn(player1, new KillerService());
        harness.passBothPriorities();
        Permanent food = findPermanent(player1, "Food");
        harness.setLife(player1, 10);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(food), null, null);

        assertThat(countPermanents(player1, "Food")).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.assertLife(player1, 10);
        harness.passBothPriorities();
        harness.assertLife(player1, 13);
    }

    @Test
    @DisplayName("Insufficient mana does not sacrifice the token or create a Rhino")
    void cannotPayWithOnlyOneMana() {
        harness.enterBattlefieldAndReturn(player1, new KillerService());
        harness.passBothPriorities();
        advanceToEndStep();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(countPermanents(player1, "Food")).isOne();
        assertThat(countPermanents(player1, "Rhino Warrior")).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isOne();
    }

    @Test
    @DisplayName("A nontoken enchantment cannot be sacrificed for the end-step ability")
    void doesNothingWithoutTokens() {
        harness.addToBattlefield(player1, new KillerService());
        advanceToEndStep();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(countPermanents(player1, "Killer Service")).isOne();
        assertThat(countPermanents(player1, "Rhino Warrior")).isZero();
    }

    @Test
    @DisplayName("A Rhino creature token can be sacrificed instead of Food")
    void canSacrificeANonFoodToken() {
        harness.enterBattlefieldAndReturn(player1, new KillerService());
        harness.passBothPriorities();
        Permanent food = findPermanent(player1, "Food");
        advanceToEndStep();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, food.getId());
        harness.passBothPriorities();
        Permanent originalRhino = findPermanent(player1, "Rhino Warrior");

        advanceToEndStep();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, originalRhino.getId());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Food")).isZero();
        assertThat(countPermanents(player1, "Rhino Warrior")).isOne();
        assertThat(findPermanent(player1, "Rhino Warrior").getId()).isNotEqualTo(originalRhino.getId());
    }

    @Test
    @DisplayName("Does not trigger during an opponent's end step")
    void doesNotTriggerOnOpponentsEndStep() {
        harness.enterBattlefieldAndReturn(player1, new KillerService());
        harness.passBothPriorities();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(countPermanents(player1, "Food")).isOne();
        assertThat(countPermanents(player1, "Rhino Warrior")).isZero();
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.passBothPriorities();
    }
}
