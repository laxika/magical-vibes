package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IrohTeaMaster.class, GrizzlyBears.class})
class IrohTeaMasterTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Food token when it enters")
    void createsFoodTokenWhenItEnters() {
        harness.setHand(player1, List.of(new IrohTeaMaster()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Food")).hasSize(1);
    }

    @Test
    @DisplayName("Giving away a permanent creates an Ally with counters for owned permanents")
    void givingAwayPermanentCreatesAllyWithCounters() {
        harness.addToBattlefield(player1, new IrohTeaMaster());
        Permanent donated = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        advanceToCombat(player1);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handlePermanentChosen(player1, donated.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(donated);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(donated);
        Permanent ally = findPermanent(player1, "Ally");
        assertThat(ally.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining the ability does not give away a permanent or create an Ally")
    void decliningAbilityDoesNothing() {
        harness.addToBattlefield(player1, new IrohTeaMaster());
        Permanent donated = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handlePermanentChosen(player1, donated.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(donated);
        assertThat(findPermanents(player1, "Ally")).isEmpty();
    }

    @Test
    @DisplayName("Counts previously donated permanents as well as the current donation")
    void countsAllOwnedPermanentsOpponentsControl() {
        harness.addToBattlefield(player1, new IrohTeaMaster());
        GrizzlyBears previouslyDonated = new GrizzlyBears();
        previouslyDonated.setOwnerId(player1.getId());
        harness.addToBattlefield(player2, previouslyDonated);
        Permanent donated = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handlePermanentChosen(player1, donated.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Ally").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(2);
    }

    @Test
    @DisplayName("Donating a permanent owned by the opponent creates an Ally without counters")
    void donatedPermanentOwnedByOpponentDoesNotCount() {
        harness.addToBattlefield(player1, new IrohTeaMaster());
        GrizzlyBears borrowed = new GrizzlyBears();
        borrowed.setOwnerId(player2.getId());
        Permanent donated = harness.addToBattlefieldAndReturn(player1, borrowed);

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handlePermanentChosen(player1, donated.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(donated);
        assertThat(findPermanent(player1, "Ally").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isZero();
    }

    @Test
    @DisplayName("Donating Iroh himself still creates the Ally for the original controller")
    void donatingIrohCreatesAllyForOriginalController() {
        Permanent iroh = harness.addToBattlefieldAndReturn(player1, new IrohTeaMaster());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handlePermanentChosen(player1, iroh.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(iroh);
        assertThat(findPermanents(player1, "Ally")).isEmpty();
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Ally").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(1);
        assertThat(findPermanents(player2, "Ally")).isEmpty();
    }

    @Test
    @DisplayName("Sacrificing donated Food in response to the reflexive trigger leaves an Ally without counters")
    void countsOwnedPermanentsWhenReflexiveTriggerResolves() {
        harness.enterBattlefieldAndReturn(player1, new IrohTeaMaster());
        resolveAllTriggers();
        Permanent food = findPermanent(player1, "Food");

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handlePermanentChosen(player1, food.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findPermanents(player1, "Ally")).isEmpty();
        assertThat(gd.stack).hasSize(1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(food), 0, null, null);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore + 3);
        assertThat(findPermanents(player2, "Food")).isEmpty();
        assertThat(findPermanent(player1, "Ally").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isZero();
    }

    @Test
    @DisplayName("A sacrificed donation target does not create an Ally")
    void sacrificedDonationTargetDoesNotCreateAlly() {
        harness.enterBattlefieldAndReturn(player1, new IrohTeaMaster());
        resolveAllTriggers();
        Permanent food = findPermanent(player1, "Food");

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handlePermanentChosen(player1, food.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(food), 0, null, null);
        resolveAllTriggers();
        if (gd.interaction.isAwaitingInput()) {
            harness.handleMayAbilityChosen(player1, true);
            resolveAllTriggers();
        }

        assertThat(findPermanents(player1, "Food")).isEmpty();
        assertThat(findPermanents(player2, "Food")).isEmpty();
        assertThat(findPermanents(player1, "Ally")).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger at the beginning of the opponent's combat")
    void doesNotTriggerDuringOpponentsCombat() {
        harness.addToBattlefield(player1, new IrohTeaMaster());

        advanceToCombat(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(findPermanents(player1, "Ally")).isEmpty();
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
